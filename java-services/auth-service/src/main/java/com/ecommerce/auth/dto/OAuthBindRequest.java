package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth Bind Request DTO
 * 
 * Request to bind third-party platform account to existing user account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthBindRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Platform is required")
    private String platform;
    
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    private String state;
}