package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.BindPhoneRequest;
import com.ecommerce.auth.dto.ChangePasswordRequest;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.PhoneLoginRequest;
import com.ecommerce.auth.dto.PhoneRegisterRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * REST API endpoints for user authentication.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * User login endpoint
     * 
     * POST /auth/login
     * Request body: { "username": "admin", "password": "admin123" }
     * Response: { "token": "jwt-token", "tokenType": "Bearer", "expiresIn": 86400000, "username": "admin", "role": "ADMIN", "message": "Login successful" }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        
        LoginResponse response = authService.login(request);
        
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * User registration endpoint
     * 
     * POST /auth/register
     * Request body: { "username": "newuser", "password": "password123", "email": "user@example.com", "role": "USER" }
     * Response: { "token": "jwt-token", "tokenType": "Bearer", "expiresIn": 86400000, "username": "newuser", "role": "USER", "message": "Login successful" }
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for user: {}", request.getUsername());
        
        LoginResponse response = authService.register(request);
        
        if (response.getToken() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Phone number registration endpoint
     * 
     * POST /auth/register-phone
     * Request body: { "phone": "+8613800138000", "code": "123456", "password": "optional" }
     * Response: { "token": "jwt-token", "tokenType": "Bearer", "expiresIn": 86400000, "username": "phone_...", "role": "USER", "message": "Login successful" }
     */
    @PostMapping("/register-phone")
    public ResponseEntity<LoginResponse> registerByPhone(@Valid @RequestBody PhoneRegisterRequest request) {
        log.info("Phone registration request received for phone: {}", maskPhone(request.getPhone()));
        
        LoginResponse response = authService.registerByPhone(request);
        
        if (response.getToken() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Phone number login endpoint
     * 
     * POST /auth/login-phone
     * Request body: { "phone": "+8613800138000", "code": "123456" }
     * Response: { "token": "jwt-token", "tokenType": "Bearer", "expiresIn": 86400000, "username": "...", "role": "USER", "message": "Login successful" }
     */
    @PostMapping("/login-phone")
    public ResponseEntity<LoginResponse> loginByPhone(@Valid @RequestBody PhoneLoginRequest request) {
        log.info("Phone login request received for phone: {}", maskPhone(request.getPhone()));
        
        LoginResponse response = authService.loginByPhone(request);
        
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * Bind phone number to account endpoint
     * 
     * POST /auth/bind-phone
     * Request body: { "username": "admin", "phone": "+8613800138000", "code": "123456" }
     * Response: { "message": "Phone bound successfully" }
     */
    @PostMapping("/bind-phone")
    public ResponseEntity<Map<String, String>> bindPhone(@Valid @RequestBody BindPhoneRequest request) {
        log.info("Bind phone request received for user: {}", request.getUsername());
        
        boolean success = authService.bindPhone(request);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Phone bound successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to bind phone. Please check verification code or phone availability."));
        }
    }
    
    /**
     * Change password endpoint
     * 
     * POST /auth/change-password
     * Request body: { "username": "admin", "oldPassword": "oldpass", "newPassword": "newpass" }
     * Response: { "message": "Password changed successfully" }
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request received for user: {}", request.getUsername());
        
        boolean success = authService.changePassword(request);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to change password. Please check your credentials."));
        }
    }
    
    /**
     * User logout endpoint
     * 
     * POST /auth/logout
     * Header: Authorization: Bearer {token}
     * Response: { "message": "Logout successful" }
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Logout request received");
        
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Authorization header is required"));
        }
        
        boolean success = authService.logout(authHeader);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
        }
    }
    
    /**
     * Token validation endpoint
     * 
     * GET /auth/validate
     * Header: Authorization: Bearer {token}
     * Response: { "valid": true, "username": "admin", "role": "ADMIN" }
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Token validation request received");
        
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Authorization header is required"
            ));
        }
        
        String username = authService.validateToken(authHeader);
        String role = authService.getUserRole(authHeader);
        
        if (username != null) {
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "role", role
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "valid", false,
                    "message", "Invalid or expired token"
            ));
        }
    }
    
    /**
     * Get user info endpoint
     * 
     * GET /auth/user/{username}
     * Response: { "id": 1, "username": "admin", "email": "admin@ecommerce.com", "role": "ADMIN", "enabled": true }
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username) {
        log.info("Get user info request for: {}", username);
        
        Optional<?> userOpt = authService.getUserByUsername(username);
        
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
    }
    
    /**
     * Health check endpoint
     * 
     * GET /auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth-service"
        ));
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}