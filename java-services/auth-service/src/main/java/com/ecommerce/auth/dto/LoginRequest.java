package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * 
 * Contains user credentials for authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}