package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth Status Response DTO
 * 
 * Response showing OAuth binding status for a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthStatusResponse {
    
    private String platform;
    
    private boolean bound;
    
    private String openid;
    
    private String displayName;
}