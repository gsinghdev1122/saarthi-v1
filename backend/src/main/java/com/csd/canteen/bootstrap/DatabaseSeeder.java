package com.csd.canteen.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.csd.canteen.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
//        userRepository.findByUsername("admin").ifPresent(user -> {
//            // Force reset the password using your app's exact live encoder
//            user.setPasswordHash(passwordEncoder.encode("admin123")); 
//            userRepository.save(user);
//            System.out.println("🚀 ADMIN PASSWORD RESET TO: admin123");
//        });
    }
}
