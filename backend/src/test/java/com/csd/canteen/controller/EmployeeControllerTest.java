package com.csd.canteen.controller;

import com.csd.canteen.dto.request.CreateEmployeeRequest;
import com.csd.canteen.dto.response.EmployeeResponse;
import com.csd.canteen.exception.ConflictException;
import com.csd.canteen.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// addFilters = false: these tests exercise controller/validation/error-handling
// logic, not the security filter chain itself (see SecurityConfigIntegrationTest
// for a real end-to-end auth check).
@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EmployeeService employeeService;

    @Test
    void listReturnsEmployeesAsJson() throws Exception {
        EmployeeResponse response = new EmployeeResponse(1L, "Test", "CSD-001", "Permanent",
                "Clerk", new BigDecimal("95.00"), null, "active", "Delhi Cantt");
        when(employeeService.listAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeCode").value("CSD-001"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        CreateEmployeeRequest invalid = new CreateEmployeeRequest("", "CSD-002", "Permanent", "Clerk", null);

        mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createReturns409OnDuplicateEmployeeCode() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest("Test", "CSD-001", "Permanent", "Clerk", null);
        when(employeeService.create(any())).thenThrow(new ConflictException("An employee with code 'CSD-001' already exists"));

        mockMvc.perform(post("/api/employees")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An employee with code 'CSD-001' already exists"));
    }
}
