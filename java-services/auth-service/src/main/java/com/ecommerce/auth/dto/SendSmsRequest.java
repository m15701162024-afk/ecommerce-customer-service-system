package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Send SMS Code Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendSmsRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone number format")
    private String phone;
}