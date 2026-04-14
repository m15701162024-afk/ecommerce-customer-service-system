package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth Authorize Response DTO
 * 
 * Response containing OAuth authorization URL for redirect.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAuthorizeResponse {
    
    private String authorizeUrl;
    
    private String platform;
    
    private String state;
}