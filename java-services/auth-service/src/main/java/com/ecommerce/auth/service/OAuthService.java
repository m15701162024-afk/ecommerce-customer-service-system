package com.ecommerce.auth.service;

import com.ecommerce.auth.config.OAuthProperties;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.OAuthAuthorizeResponse;
import com.ecommerce.auth.dto.OAuthBindRequest;
import com.ecommerce.auth.dto.OAuthStatusResponse;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth Service
 * 
 * Handles third-party platform OAuth authorization, binding, and login.
 * Supports Douyin (抖音), Xiaohongshu (小红书), and 1688 platforms.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    
    private final OAuthProperties oAuthProperties;
    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String OAUTH_STATE_PREFIX = "oauth:state:";
    private static final long STATE_EXPIRE_SECONDS = 300;
    
    /**
     * Generate OAuth authorization URL for platform
     */
    public OAuthAuthorizeResponse generateAuthorizeUrl(String platform) {
        log.info("Generating authorize URL for platform: {}", platform);
        
        if (!oAuthProperties.hasPlatform(platform)) {
            log.warn("Platform not configured: {}", platform);
            throw new IllegalArgumentException("Platform not configured: " + platform);
        }
        
        OAuthProperties.PlatformConfig config = oAuthProperties.getPlatformConfig(platform);
        
        String state = UUID.randomUUID().toString().replace("-", "");
        
        redisTemplate.opsForValue().set(
            OAUTH_STATE_PREFIX + state, 
            platform, 
            Duration.ofSeconds(STATE_EXPIRE_SECONDS)
        );
        
        String authorizeUrl = buildAuthorizeUrl(platform, config, state);
        
        log.info("Generated authorize URL for {}: {}", platform, authorizeUrl);
        
        return OAuthAuthorizeResponse.builder()
            .authorizeUrl(authorizeUrl)
            .platform(platform)
            .state(state)
            .build();
    }
    
    private String buildAuthorizeUrl(String platform, OAuthProperties.PlatformConfig config, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(config.getAuthorizeUrl())
            .queryParam("client_id", config.getAppId())
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", config.getRedirectUri())
            .queryParam("state", state);
        
        switch (platform) {
            case OAuthProperties.PLATFORM_DOUYIN:
                return builder.build().encode().toUriString();
            case OAuthProperties.PLATFORM_XIAOHONGSHU:
                return builder.build().encode().toUriString();
            case OAuthProperties.PLATFORM_1688:
                builder.queryParam("client_id", config.getAppId());
                return builder.build().encode().toUriString();
            default:
                return builder.build().encode().toUriString();
        }
    }
    
    /**
     * Handle OAuth callback and get openid from platform
     */
    @Transactional
    public String handleCallback(String platform, String code, String state) {
        log.info("Handling OAuth callback for platform: {} with code: {}", platform, code);
        
        String storedPlatform = (String) redisTemplate.opsForValue().get(OAUTH_STATE_PREFIX + state);
        if (storedPlatform == null || !storedPlatform.equals(platform)) {
            log.warn("Invalid or expired state for platform: {}", platform);
            throw new IllegalArgumentException("Invalid or expired OAuth state");
        }
        
        redisTemplate.delete(OAUTH_STATE_PREFIX + state);
        
        String openid = fetchOpenidFromPlatform(platform, code);
        
        log.info("Obtained openid for {}: {}", platform, openid);
        return openid;
    }
    
    private String fetchOpenidFromPlatform(String platform, String code) {
        OAuthProperties.PlatformConfig config = oAuthProperties.getPlatformConfig(platform);
        
        switch (platform) {
            case OAuthProperties.PLATFORM_DOUYIN:
                return fetchDouyinOpenid(config, code);
            case OAuthProperties.PLATFORM_XIAOHONGSHU:
                return fetchXiaohongshuOpenid(config, code);
            case OAuthProperties.PLATFORM_1688:
                return fetch1688Openid(config, code);
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }
    
    private String fetchDouyinOpenid(OAuthProperties.PlatformConfig config, String code) {
        log.info("Fetching Douyin openid with code: {}", code);
        
        // TODO: Call Douyin API to get token and openid
        // POST https://developer.toutiao.com/api/oauth2/access_token/
        // Parameters: client_id, client_secret, code, grant_type=authorization_code
        // Then GET https://developer.toutiao.com/api/oauth2/get_user_info/ with access_token
        // Returns openid in response
        
        String mockOpenid = "douyin_mock_" + UUID.randomUUID().toString().substring(0, 8);
        log.warn("Using mock openid for Douyin: {} - Real API implementation needed", mockOpenid);
        return mockOpenid;
    }
    
    private String fetchXiaohongshuOpenid(OAuthProperties.PlatformConfig config, String code) {
        log.info("Fetching Xiaohongshu openid with code: {}", code);
        
        // TODO: Call Xiaohongshu API to get token and openid
        // POST https://api.xiaohongshu.com/api/sns/v1/oauth2/access_token
        // Parameters: client_id, client_secret, code, grant_type=authorization_code, redirect_uri
        // Then GET user info API with access_token
        // Returns openid/user_id in response
        
        String mockOpenid = "xhs_mock_" + UUID.randomUUID().toString().substring(0, 8);
        log.warn("Using mock openid for Xiaohongshu: {} - Real API implementation needed", mockOpenid);
        return mockOpenid;
    }
    
    private String fetch1688Openid(OAuthProperties.PlatformConfig config, String code) {
        log.info("Fetching 1688 openid with code: {}", code);
        
        // TODO: Call 1688 API to get token and openid
        // POST https://auth.1688.com/oauth/token
        // Parameters: client_id, client_secret, code, grant_type=authorization_code, redirect_uri
        // Returns access_token and member_id/openid
        
        String mockOpenid = "1688_mock_" + UUID.randomUUID().toString().substring(0, 8);
        log.warn("Using mock openid for 1688: {} - Real API implementation needed", mockOpenid);
        return mockOpenid;
    }
    
    /**
     * Bind third-party account to existing user
     */
    @Transactional
    public boolean bindAccount(String username, String platform, String openid) {
        log.info("Binding {} account for user: {} with openid: {}", platform, username, openid);
        
        Optional<User> userOpt = userCacheService.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            return false;
        }
        
        if (!oAuthProperties.hasPlatform(platform)) {
            log.warn("Platform not configured: {}", platform);
            return false;
        }
        
        if (isOAuthIdBound(platform, openid)) {
            log.warn("Openid {} already bound to another account", openid);
            return false;
        }
        
        User user = userOpt.get();
        setOpenidForUser(user, platform, openid);
        
        userRepository.save(user);
        userCacheService.invalidateUserCache(username);
        userCacheService.cacheUser(user);
        
        log.info("Successfully bound {} account for user: {}", platform, username);
        return true;
    }
    
    /**
     * Bind account using authorization code
     */
    @Transactional
    public boolean bindAccountWithCode(OAuthBindRequest request) {
        log.info("Binding {} account for user: {}", request.getPlatform(), request.getUsername());
        
        String openid = handleCallback(request.getPlatform(), request.getCode(), request.getState());
        return bindAccount(request.getUsername(), request.getPlatform(), openid);
    }
    
    /**
     * Unbind third-party account from user
     */
    @Transactional
    public boolean unbindAccount(String username, String platform) {
        log.info("Unbinding {} account for user: {}", platform, username);
        
        Optional<User> userOpt = userCacheService.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            return false;
        }
        
        if (!oAuthProperties.hasPlatform(platform)) {
            log.warn("Platform not configured: {}", platform);
            return false;
        }
        
        User user = userOpt.get();
        setOpenidForUser(user, platform, null);
        
        userRepository.save(user);
        userCacheService.invalidateUserCache(username);
        userCacheService.cacheUser(user);
        
        log.info("Successfully unbound {} account for user: {}", platform, username);
        return true;
    }
    
    /**
     * Login via third-party OAuth
     */
    @Transactional
    public LoginResponse loginByOAuth(String platform, String openid) {
        log.info("OAuth login attempt for {} with openid: {}", platform, openid);
        
        Optional<User> userOpt = findUserByOpenid(platform, openid);
        
        if (userOpt.isEmpty()) {
            log.warn("User not found for {} openid: {}", platform, openid);
            return LoginResponse.failure("Third-party account not bound to any user");
        }
        
        User user = userOpt.get();
        
        if (!user.isActive()) {
            log.warn("User account is disabled for {} openid: {}", platform, openid);
            return LoginResponse.failure("Account is disabled");
        }
        
        userRepository.updateLastLoginAt(user.getUsername(), java.time.LocalDateTime.now());
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        log.info("OAuth login successful for {} openid: {} with username: {}", platform, openid, user.getUsername());
        
        return LoginResponse.success(
            token,
            jwtUtil.getExpirationTime(),
            user.getUsername(),
            user.getRole()
        );
    }
    
    /**
     * Check if OAuth ID is already bound to any user
     */
    public boolean isOAuthIdBound(String platform, String openid) {
        return findUserByOpenid(platform, openid).isPresent();
    }
    
    /**
     * Find user by OAuth openid
     */
    public Optional<User> findUserByOpenid(String platform, String openid) {
        switch (platform) {
            case OAuthProperties.PLATFORM_DOUYIN:
                return userRepository.findByDouyinOpenid(openid);
            case OAuthProperties.PLATFORM_XIAOHONGSHU:
                return userRepository.findByXiaohongshuId(openid);
            case OAuthProperties.PLATFORM_1688:
                return userRepository.findByAliId(openid);
            default:
                return Optional.empty();
        }
    }
    
    /**
     * Set openid for user based on platform
     */
    private void setOpenidForUser(User user, String platform, String openid) {
        switch (platform) {
            case OAuthProperties.PLATFORM_DOUYIN:
                user.setDouyinOpenid(openid);
                break;
            case OAuthProperties.PLATFORM_XIAOHONGSHU:
                user.setXiaohongshuId(openid);
                break;
            case OAuthProperties.PLATFORM_1688:
                user.setAliId(openid);
                break;
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }
    
    /**
     * Get OAuth binding status for user
     */
    public List<OAuthStatusResponse> getOAuthStatus(String username) {
        log.info("Getting OAuth status for user: {}", username);
        
        Optional<User> userOpt = userCacheService.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        User user = userOpt.get();
        List<OAuthStatusResponse> statusList = new ArrayList<>();
        
        if (oAuthProperties.hasPlatform(OAuthProperties.PLATFORM_DOUYIN)) {
            statusList.add(OAuthStatusResponse.builder()
                .platform(OAuthProperties.PLATFORM_DOUYIN)
                .bound(user.getDouyinOpenid() != null)
                .openid(user.getDouyinOpenid())
                .displayName("抖音")
                .build());
        }
        
        if (oAuthProperties.hasPlatform(OAuthProperties.PLATFORM_XIAOHONGSHU)) {
            statusList.add(OAuthStatusResponse.builder()
                .platform(OAuthProperties.PLATFORM_XIAOHONGSHU)
                .bound(user.getXiaohongshuId() != null)
                .openid(user.getXiaohongshuId())
                .displayName("小红书")
                .build());
        }
        
        if (oAuthProperties.hasPlatform(OAuthProperties.PLATFORM_1688)) {
            statusList.add(OAuthStatusResponse.builder()
                .platform(OAuthProperties.PLATFORM_1688)
                .bound(user.getAliId() != null)
                .openid(user.getAliId())
                .displayName("1688")
                .build());
        }
        
        return statusList;
    }
    
    /**
     * Get OAuth binding status for a specific platform
     */
    public OAuthStatusResponse getOAuthStatusForPlatform(String username, String platform) {
        log.info("Getting OAuth status for user: {} platform: {}", username, platform);
        
        Optional<User> userOpt = userCacheService.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            return OAuthStatusResponse.builder()
                .platform(platform)
                .bound(false)
                .build();
        }
        
        User user = userOpt.get();
        boolean bound = false;
        String openid = null;
        String displayName = null;
        
        switch (platform) {
            case OAuthProperties.PLATFORM_DOUYIN:
                bound = user.getDouyinOpenid() != null;
                openid = user.getDouyinOpenid();
                displayName = "抖音";
                break;
            case OAuthProperties.PLATFORM_XIAOHONGSHU:
                bound = user.getXiaohongshuId() != null;
                openid = user.getXiaohongshuId();
                displayName = "小红书";
                break;
            case OAuthProperties.PLATFORM_1688:
                bound = user.getAliId() != null;
                openid = user.getAliId();
                displayName = "1688";
                break;
            default:
                return OAuthStatusResponse.builder()
                    .platform(platform)
                    .bound(false)
                    .build();
        }
        
        return OAuthStatusResponse.builder()
            .platform(platform)
            .bound(bound)
            .openid(openid)
            .displayName(displayName)
            .build();
    }
}