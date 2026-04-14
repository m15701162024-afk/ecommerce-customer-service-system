package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phone Register Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRegisterRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone number format")
    private String phone;
    
    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
    private String code;
    
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
    
    @Size(max = 20, message = "Role must not exceed 20 characters")
    @Builder.Default
    private String role = "USER";
}