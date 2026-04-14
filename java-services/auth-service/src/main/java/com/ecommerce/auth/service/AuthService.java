package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.BindPhoneRequest;
import com.ecommerce.auth.dto.ChangePasswordRequest;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.PhoneLoginRequest;
import com.ecommerce.auth.dto.PhoneRegisterRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

/**
 * Authentication Service
 * 
 * Handles user authentication logic including login, registration, and token management.
 * Uses database for persistence and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SmsService smsService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";
    
    /**
     * Authenticate user and generate JWT token
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        // Get user from cache or database
        Optional<User> userOpt = userCacheService.getUserByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", request.getUsername());
            return LoginResponse.failure("Invalid username or password");
        }
        
        User user = userOpt.get();
        
        // Check if user is active
        if (!user.isActive()) {
            log.warn("User account is disabled: {}", request.getUsername());
            return LoginResponse.failure("Account is disabled");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            return LoginResponse.failure("Invalid username or password");
        }
        
        // Update last login time
        userRepository.updateLastLoginAt(user.getUsername(), LocalDateTime.now());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        log.info("Login successful for user: {} with role: {}", user.getUsername(), user.getRole());
        
        return LoginResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getRole()
        );
    }
    
    /**
     * Register new user
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            return LoginResponse.failure("Username already exists");
        }
        
        // Check if email already exists (if provided)
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
                && userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            return LoginResponse.failure("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole() != null ? request.getRole() : "USER")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        user = userRepository.save(user);
        
        // Cache the new user
        userCacheService.cacheUser(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        log.info("Registration successful for user: {} with role: {}", user.getUsername(), user.getRole());
        
        return LoginResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getRole()
        );
    }
    
    /**
     * Change user password
     */
    @Transactional
    public boolean changePassword(ChangePasswordRequest request) {
        log.info("Password change attempt for user: {}", request.getUsername());
        
        // Get user from cache or database
        Optional<User> userOpt = userCacheService.getUserByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", request.getUsername());
            return false;
        }
        
        User user = userOpt.get();
        
        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Invalid old password for user: {}", request.getUsername());
            return false;
        }
        
        // Update password
        String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
        userRepository.updatePassword(user.getUsername(), newEncodedPassword);
        
        // Invalidate cache
        userCacheService.invalidateUserCache(user.getUsername());
        
        log.info("Password changed successfully for user: {}", user.getUsername());
        return true;
    }
    
    /**
     * Logout user by blacklisting token
     */
    public boolean logout(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            String username = jwtUtil.extractUsername(token);
            Date expiration = jwtUtil.extractExpiration(token);
            
            // Calculate remaining TTL
            long ttlMs = expiration.getTime() - System.currentTimeMillis();
            if (ttlMs <= 0) {
                log.debug("Token already expired, no need to blacklist");
                return true;
            }
            
            // Add to Redis blacklist with TTL
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, username, Duration.ofMillis(ttlMs));
            
            log.info("User {} logged out successfully", username);
            return true;
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }
    
    /**
     * Validate token and extract username
     */
    public String validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (isTokenBlacklisted(token)) {
            log.warn("Token is blacklisted");
            return null;
        }
        
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                
                // Verify user still exists and is active
                Optional<User> userOpt = userCacheService.getUserByUsername(username);
                if (userOpt.isPresent() && userOpt.get().isActive()) {
                    return username;
                }
            }
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get user role from token
     */
    public String getUserRole(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return jwtUtil.extractRole(token);
        } catch (Exception e) {
            log.error("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userCacheService.getUserByUsername(username);
    }
    
    /**
     * Register user with phone number and verification code
     */
    @Transactional
    public LoginResponse registerByPhone(PhoneRegisterRequest request) {
        log.info("Phone registration attempt for: {}", maskPhone(request.getPhone()));
        
        if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
            log.warn("Invalid verification code for phone: {}", maskPhone(request.getPhone()));
            return LoginResponse.failure("Invalid or expired verification code");
        }
        
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Phone already registered: {}", maskPhone(request.getPhone()));
            return LoginResponse.failure("Phone number already registered");
        }
        
        String username = "phone_" + request.getPhone().replaceAll("[^\\d]", "");
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + System.currentTimeMillis();
        }
        
        User user = User.builder()
                .username(username)
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : "")
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : "USER")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        user = userRepository.save(user);
        userCacheService.cacheUser(user);
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        log.info("Phone registration successful for: {} with username: {}", maskPhone(request.getPhone()), user.getUsername());
        
        return LoginResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getRole()
        );
    }
    
    /**
     * Login with phone number and verification code
     */
    @Transactional
    public LoginResponse loginByPhone(PhoneLoginRequest request) {
        log.info("Phone login attempt for: {}", maskPhone(request.getPhone()));
        
        if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
            log.warn("Invalid verification code for phone: {}", maskPhone(request.getPhone()));
            return LoginResponse.failure("Invalid or expired verification code");
        }
        
        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        
        if (userOpt.isEmpty()) {
            log.warn("User not found for phone: {}", maskPhone(request.getPhone()));
            return LoginResponse.failure("Phone number not registered");
        }
        
        User user = userOpt.get();
        
        if (!user.isActive()) {
            log.warn("User account is disabled for phone: {}", maskPhone(request.getPhone()));
            return LoginResponse.failure("Account is disabled");
        }
        
        userRepository.updateLastLoginAt(user.getUsername(), LocalDateTime.now());
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        log.info("Phone login successful for: {} with username: {}", maskPhone(request.getPhone()), user.getUsername());
        
        return LoginResponse.success(
                token,
                jwtUtil.getExpirationTime(),
                user.getUsername(),
                user.getRole()
        );
    }
    
    /**
     * Bind phone number to existing user account
     */
    @Transactional
    public boolean bindPhone(BindPhoneRequest request) {
        log.info("Bind phone request for user: {} to phone: {}", request.getUsername(), maskPhone(request.getPhone()));
        
        if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
            log.warn("Invalid verification code for binding phone");
            return false;
        }
        
        Optional<User> userOpt = userCacheService.getUserByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", request.getUsername());
            return false;
        }
        
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Phone already bound to another account: {}", maskPhone(request.getPhone()));
            return false;
        }
        
        User user = userOpt.get();
        user.setPhone(request.getPhone());
        userRepository.save(user);
        userCacheService.invalidateUserCache(user.getUsername());
        userCacheService.cacheUser(user);
        
        log.info("Phone bound successfully for user: {}", user.getUsername());
        return true;
    }
    
    /**
     * Get user by phone number
     */
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "****" + phone.substring(phone.length() - 4);
    }
}