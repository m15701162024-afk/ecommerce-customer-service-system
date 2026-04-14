package com.ecommerce.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth Configuration Properties
 * 
 * Configuration for third-party platform OAuth integrations.
 * Supports Douyin (抖音), Xiaohongshu (小红书), and 1688.
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {
    
    private Map<String, PlatformConfig> platforms = new HashMap<>();
    
    /**
     * Platform-specific OAuth configuration
     */
    @Data
    public static class PlatformConfig {
        /**
         * Platform application ID (AppId/AppKey)
         */
        private String appId;
        
        /**
         * Platform application secret (AppSecret)
         */
        private String appSecret;
        
        /**
         * Redirect URI for OAuth callback
         */
        private String redirectUri;
        
        /**
         * OAuth authorization URL
         */
        private String authorizeUrl;
        
        /**
         * OAuth token URL
         */
        private String tokenUrl;
        
        /**
         * OAuth user info URL (for getting openid)
         */
        private String userInfoUrl;
        
        /**
         * Platform display name
         */
        private String displayName;
    }
    
    /**
     * Get platform configuration by platform name
     */
    public PlatformConfig getPlatformConfig(String platform) {
        return platforms.get(platform);
    }
    
    /**
     * Check if platform is configured
     */
    public boolean hasPlatform(String platform) {
        return platforms.containsKey(platform) && platforms.get(platform) != null;
    }
    
    /**
     * Valid OAuth platform names
     */
    public static final String PLATFORM_DOUYIN = "douyin";
    public static final String PLATFORM_XIAOHONGSHU = "xiaohongshu";
    public static final String PLATFORM_1688 = "a1688";
    
    /**
     * Get User entity field name for platform openid
     */
    public static String getOpenidField(String platform) {
        switch (platform) {
            case PLATFORM_DOUYIN:
                return "douyinOpenid";
            case PLATFORM_XIAOHONGSHU:
                return "xiaohongshuId";
            case PLATFORM_1688:
                return "aliId";
            default:
                return null;
        }
    }
}