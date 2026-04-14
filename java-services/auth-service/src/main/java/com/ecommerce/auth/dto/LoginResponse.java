package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 * 
 * Contains JWT token and user information after successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String tokenType;
    private long expiresIn;
    private String username;
    private String role;
    private String message;
    
    public static LoginResponse success(String token, long expiresIn, String username, String role) {
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .username(username)
                .role(role)
                .message("Login successful")
                .build();
    }
    
    public static LoginResponse failure(String message) {
        return LoginResponse.builder()
                .message(message)
                .build();
    }
}