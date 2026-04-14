package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Change Password Request DTO
 * 
 * Contains old and new password for password change operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Old password is required")
    private String oldPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    private String newPassword;
}