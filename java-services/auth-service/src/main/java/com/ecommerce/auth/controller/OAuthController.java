package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.OAuthAuthorizeResponse;
import com.ecommerce.auth.dto.OAuthBindRequest;
import com.ecommerce.auth.dto.OAuthCallbackRequest;
import com.ecommerce.auth.dto.OAuthStatusResponse;
import com.ecommerce.auth.service.OAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OAuth Controller
 * 
 * REST API endpoints for third-party platform OAuth binding and login.
 * Supports Douyin (抖音), Xiaohongshu (小红书), and 1688 platforms.
 */
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {
    
    private final OAuthService oAuthService;
    
    /**
     * Get OAuth authorization URL for platform
     * 
     * GET /oauth/{platform}/authorize
     * Response: { "authorizeUrl": "...", "platform": "douyin", "state": "..." }
     */
    @GetMapping("/{platform}/authorize")
    public ResponseEntity<OAuthAuthorizeResponse> getAuthorizeUrl(@PathVariable String platform) {
        log.info("Get authorize URL request for platform: {}", platform);
        
        try {
            OAuthAuthorizeResponse response = oAuthService.generateAuthorizeUrl(platform);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid platform requested: {}", platform);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Handle OAuth callback from platform
     * 
     * GET /oauth/{platform}/callback?code=xxx&state=xxx
     * Response: { "openid": "...", "platform": "douyin" }
     */
    @GetMapping("/{platform}/callback")
    public ResponseEntity<Map<String, String>> handleCallback(
            @PathVariable String platform,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        log.info("OAuth callback received for platform: {} with code: {}", platform, code);
        
        try {
            String openid = oAuthService.handleCallback(platform, code, state);
            return ResponseEntity.ok(Map.of(
                "openid", openid,
                "platform", platform
            ));
        } catch (IllegalArgumentException e) {
            log.warn("OAuth callback failed for platform {}: {}", platform, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Bind third-party account to existing user
     * 
     * POST /oauth/{platform}/bind
     * Request body: { "username": "admin", "code": "xxx", "state": "xxx" }
     * Response: { "message": "Account bound successfully" }
     */
    @PostMapping("/{platform}/bind")
    public ResponseEntity<Map<String, String>> bindAccount(
            @PathVariable String platform,
            @Valid @RequestBody OAuthBindRequest request) {
        log.info("Bind {} account request for user: {}", platform, request.getUsername());
        
        request.setPlatform(platform);
        
        boolean success = oAuthService.bindAccountWithCode(request);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Account bound successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to bind account. Please check authorization."));
        }
    }
    
    /**
     * Unbind third-party account from user
     * 
     * DELETE /oauth/{platform}/unbind
     * Request body: { "username": "admin" }
     * Response: { "message": "Account unbound successfully" }
     */
    @DeleteMapping("/{platform}/unbind")
    public ResponseEntity<Map<String, String>> unbindAccount(
            @PathVariable String platform,
            @RequestBody Map<String, String> request) {
        log.info("Unbind {} account request", platform);
        
        String username = request.get("username");
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Username is required"));
        }
        
        boolean success = oAuthService.unbindAccount(username, platform);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Account unbound successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Failed to unbind account"));
        }
    }
    
    /**
     * Login via third-party OAuth
     * 
     * POST /oauth/{platform}/login
     * Request body: { "openid": "xxx" }
     * Response: { "token": "...", "tokenType": "Bearer", "expiresIn": ..., "username": "...", "role": "USER" }
     */
    @PostMapping("/{platform}/login")
    public ResponseEntity<LoginResponse> loginByOAuth(
            @PathVariable String platform,
            @RequestBody Map<String, String> request) {
        log.info("OAuth login request for platform: {}", platform);
        
        String openid = request.get("openid");
        if (openid == null || openid.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(LoginResponse.failure("Openid is required"));
        }
        
        LoginResponse response = oAuthService.loginByOAuth(platform, openid);
        
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * Get OAuth binding status for user (all platforms)
     * 
     * GET /oauth/status
     * Request body: { "username": "admin" }
     * Response: [ { "platform": "douyin", "bound": true, "displayName": "抖音" }, ... ]
     */
    @GetMapping("/status")
    public ResponseEntity<List<OAuthStatusResponse>> getOAuthStatus(
            @RequestParam String username) {
        log.info("Get OAuth status request for user: {}", username);
        
        List<OAuthStatusResponse> statusList = oAuthService.getOAuthStatus(username);
        return ResponseEntity.ok(statusList);
    }
    
    /**
     * Get OAuth binding status for specific platform
     * 
     * GET /oauth/{platform}/status
     * Request body: { "username": "admin" }
     * Response: { "platform": "douyin", "bound": true, "displayName": "抖音" }
     */
    @GetMapping("/{platform}/status")
    public ResponseEntity<OAuthStatusResponse> getOAuthStatusForPlatform(
            @PathVariable String platform,
            @RequestParam String username) {
        log.info("Get OAuth status request for user: {} platform: {}", username, platform);
        
        OAuthStatusResponse status = oAuthService.getOAuthStatusForPlatform(username, platform);
        return ResponseEntity.ok(status);
    }
    
    /**
     * Health check for OAuth service
     * 
     * GET /oauth/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "oauth-service"
        ));
    }
}