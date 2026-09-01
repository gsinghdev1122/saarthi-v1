package com.csd.canteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Canteen SAARTHI backend API.
 *
 * Run locally with: mvn spring-boot:run -Dspring-boot.run.profiles=local
 * Or from Eclipse: run this class as a "Java Application" with VM arg
 * -Dspring.profiles.active=local
 */
@SpringBootApplication(scanBasePackages={"com.csd"})
public class CanteenApplication {
    public static void main(String[] args) {
        SpringApplication.run(CanteenApplication.class, args);
    }
}
