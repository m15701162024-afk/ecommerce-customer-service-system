package com.ecommerce.platform.a1688.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1688开放平台配置类
 * 
 * 配置项包括：
 * - 应用凭证（AppKey, AppSecret）
 * - API端点URL
 * - OAuth2.0回调地址
 * - 重试、熔断、限流配置
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "a1688")
public class A1688Config {
    
    /**
     * 应用AppKey
     */
    @NotBlank(message = "AppKey不能为空")
    private String appKey;
    
    /**
     * 应用AppSecret
     */
    @NotBlank(message = "AppSecret不能为空")
    private String appSecret;
    
    /**
     * API基础URL
     */
    @NotBlank(message = "API URL不能为空")
    private String apiUrl = "https://gw.open.1688.com/openapi";
    
    /**
     * OAuth2.0授权URL
     */
    private String authUrl = "https://auth.1688.com/oauth/authorize";
    
    /**
     * Token获取URL
     */
    private String tokenUrl = "https://gw.open.1688.com/openapi/token";
    
    /**
     * OAuth2.0回调地址
     */
    @NotBlank(message = "回调地址不能为空")
    private String callbackUrl;
    
    /**
     * 店铺Access Token缓存
     * Key: shopId, Value: accessToken
     */
    private final Map<Long, AccessTokenInfo> shopTokens = new ConcurrentHashMap<>();
    
    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();
    
    /**
     * 熔断配置
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
    
    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();
    
    /**
     * 获取店铺的Access Token
     * 
     * @param shopId 店铺ID
     * @return Access Token信息
     */
    public AccessTokenInfo getAccessToken(Long shopId) {
        return shopTokens.get(shopId);
    }
    
    /**
     * 设置店铺的Access Token
     * 
     * @param shopId 店铺ID
     * @param tokenInfo Token信息
     */
    public void setAccessToken(Long shopId, AccessTokenInfo tokenInfo) {
        shopTokens.put(shopId, tokenInfo);
    }
    
    /**
     * 移除店铺的Access Token
     * 
     * @param shopId 店铺ID
     */
    public void removeAccessToken(Long shopId) {
        shopTokens.remove(shopId);
    }
    
    /**
     * Access Token信息
     */
    @Data
    public static class AccessTokenInfo {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private Long createTime;
        private String memberId;
        
        /**
         * 检查Token是否过期
         * 提前5分钟认为过期，避免临界情况
         * 
         * @return 是否过期
         */
        public boolean isExpired() {
            if (createTime == null || expiresIn == null) {
                return true;
            }
            long expireTime = createTime + expiresIn * 1000 - 5 * 60 * 1000;
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 最大重试次数
         */
        @NotNull
        private Integer maxAttempts = 3;
        
        /**
         * 初始重试间隔(毫秒)
         */
        @NotNull
        private Long initialInterval = 1000L;
        
        /**
         * 最大重试间隔(毫秒)
         */
        @NotNull
        private Long maxInterval = 10000L;
        
        /**
         * 重试间隔乘数
         */
        @NotNull
        private Double multiplier = 2.0;
    }
    
    /**
     * 熔断配置
     */
    @Data
    public static class CircuitBreakerConfig {
        /**
         * 熔断器开启阈值(失败次数)
         */
        @NotNull
        private Integer failureThreshold = 5;
        
        /**
         * 熔断器半开启状态等待时间(毫秒)
         */
        @NotNull
        private Long waitDurationInOpenState = 30000L;
        
        /**
         * 半开启状态允许的调用次数
         */
        @NotNull
        private Integer permittedCallsInHalfOpenState = 3;
    }
    
    /**
     * 限流配置
     */
    @Data
    public static class RateLimitConfig {
        /**
         * 每秒最大请求数
         */
        @NotNull
        private Integer requestsPerSecond = 100;
        
        /**
         * 限流等待超时时间(毫秒)
         */
        @NotNull
        private Long timeout = 5000L;
    }
}