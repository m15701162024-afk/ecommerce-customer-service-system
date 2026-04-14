package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bind Phone Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindPhoneRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone number format")
    private String phone;
    
    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
    private String code;
}