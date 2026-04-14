package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth Callback Request DTO
 * 
 * Contains OAuth callback parameters from third-party platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthCallbackRequest {
    
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    private String state;
}