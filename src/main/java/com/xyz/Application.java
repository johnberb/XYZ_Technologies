package com.xyz;  // Must match your POM's <groupId>

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // Enables auto-configuration and component scanning
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);  // Launches Spring Boot
    }
}