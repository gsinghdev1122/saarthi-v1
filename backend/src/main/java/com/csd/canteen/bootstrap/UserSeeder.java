package com.csd.canteen.bootstrap;

import com.csd.canteen.entity.Role;
import com.csd.canteen.entity.User;
import com.csd.canteen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a single default ADMIN account on first startup if the users table
 * is empty, so there's always a way in. Credentials come from environment
 * variables (ADMIN_USERNAME / ADMIN_PASSWORD) with safe local-dev defaults —
 * in production, set ADMIN_PASSWORD to something real in your .env file and
 * change it via the API (or DB) after first login.
 */
@Component
//@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements ApplicationRunner {


    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    
    // 2. Update your constructor to receive BOTH items:
    public UserSeeder(UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:ChangeMe123!}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = User.builder()
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .displayName("Administrator")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.warn("No users found — seeded default admin account '{}'. " +
                "Log in and create real accounts, then disable/rotate this one.", adminUsername);
    }
}
