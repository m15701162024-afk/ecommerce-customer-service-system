package com.ecommerce.platform.xiaohongshu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 小红书平台配置类
 * 
 * 配置项:
 * - OAuth2.0认证参数
 * - API端点URL
 * - 回调地址
 * - 店铺Token管理
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaohongshu")
public class XiaohongshuConfig {
    
    /**
     * 小红书开放平台App ID
     */
    private String appId;
    
    /**
     * 小红书开放平台App Secret
     */
    private String appSecret;
    
    /**
     * API基础URL
     */
    private String apiUrl = "https://api.xiaohongshu.com";
    
    /**
     * OAuth2.0授权URL
     */
    private String authUrl = "https://api.xiaohongshu.com/api/sns/v1/oauth2/authorize";
    
    /**
     * Token获取URL
     */
    private String tokenUrl = "https://api.xiaohongshu.com/api/sns/v1/oauth2/token";
    
    /**
     * 回调URL
     */
    private String callbackUrl;
    
    /**
     * 店铺Token缓存 (shopId -> TokenInfo)
     */
    private final Map<Long, TokenInfo> shopTokens = new ConcurrentHashMap<>();
    
    /**
     * 获取店铺访问Token
     */
    public String getAccessToken(Long shopId) {
        TokenInfo tokenInfo = shopTokens.get(shopId);
        if (tokenInfo == null) {
            return null;
        }
        
        // 检查Token是否过期
        if (tokenInfo.isExpired()) {
            return null;
        }
        
        return tokenInfo.getAccessToken();
    }
    
    /**
     * 获取店铺刷新Token
     */
    public String getRefreshToken(Long shopId) {
        TokenInfo tokenInfo = shopTokens.get(shopId);
        return tokenInfo != null ? tokenInfo.getRefreshToken() : null;
    }
    
    /**
     * 设置店铺Token
     */
    public void setTokenInfo(Long shopId, String accessToken, String refreshToken, Long expiresIn) {
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setAccessToken(accessToken);
        tokenInfo.setRefreshToken(refreshToken);
        tokenInfo.setExpiresIn(expiresIn);
        tokenInfo.setCreatedAt(System.currentTimeMillis());
        shopTokens.put(shopId, tokenInfo);
    }
    
    /**
     * 移除店铺Token
     */
    public void removeToken(Long shopId) {
        shopTokens.remove(shopId);
    }
    
    /**
     * Token信息类
     */
    @Data
    public static class TokenInfo {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private Long createdAt;
        
        /**
         * 检查Token是否过期 (提前5分钟判定过期)
         */
        public boolean isExpired() {
            if (createdAt == null || expiresIn == null) {
                return true;
            }
            // 提前5分钟判定过期，留出刷新时间
            long expireTime = createdAt + (expiresIn - 300) * 1000;
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * 创建WebClient Bean
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}