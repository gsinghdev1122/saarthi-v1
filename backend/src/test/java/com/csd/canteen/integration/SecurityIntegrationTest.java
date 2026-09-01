package com.csd.canteen.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real end-to-end check of the auth flow: no-token requests are rejected,
 * login with the seeded admin returns a usable JWT, that JWT is accepted on
 * a protected endpoint, and role-restricted endpoints reject the wrong role.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("canteen_saarthi_test_security")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-at-least-32-bytes-long");
        registry.add("app.admin.username", () -> "admin");
        registry.add("app.admin.password", () -> "IntegrationTest123!");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void protectedEndpointRejectsRequestsWithoutAToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/api/inventory", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginWithSeededAdminReturnsAWorkingToken() {
        Map<String, String> loginBody = Map.of("username", "admin", "password", "IntegrationTest123!");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl() + "/api/auth/login", loginBody, Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) loginResponse.getBody().get("token");
        assertThat(token).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> inventoryResponse = restTemplate.exchange(
                baseUrl() + "/api/inventory", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(inventoryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        Map<String, String> loginBody = Map.of("username", "admin", "password", "totally-wrong");
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl() + "/api/auth/login", loginBody, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void nonAdminCannotCreateUsers() {
        // Admin creates an AUDITOR account, then that account is denied at /api/users.
        Map<String, String> adminLogin = Map.of("username", "admin", "password", "IntegrationTest123!");
        String adminToken = (String) restTemplate.postForEntity(baseUrl() + "/api/auth/login", adminLogin, Map.class)
                .getBody().get("token");

        HttpHeaders adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(adminToken);
        Map<String, Object> newUser = Map.of(
                "username", "auditor1", "password", "AuditorPass123!",
                "displayName", "Test Auditor", "role", "AUDITOR");
        restTemplate.exchange(baseUrl() + "/api/users", HttpMethod.POST,
                new HttpEntity<>(newUser, adminHeaders), String.class);

        Map<String, String> auditorLogin = Map.of("username", "auditor1", "password", "AuditorPass123!");
        String auditorToken = (String) restTemplate.postForEntity(baseUrl() + "/api/auth/login", auditorLogin, Map.class)
                .getBody().get("token");

        HttpHeaders auditorHeaders = new HttpHeaders();
        auditorHeaders.setBearerAuth(auditorToken);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/users", HttpMethod.GET, new HttpEntity<>(auditorHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
