package com.ecommerce.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Authentication Service Application
 * 
 * Provides JWT-based authentication for the E-Commerce Customer Service System.
 * Running on port 8083.
 * 
 * @author E-Commerce Team
 */
@SpringBootApplication
public class AuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}