package com.ecommerce.platform.douyin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
@ConfigurationProperties(prefix = "douyin")
public class DouyinConfig {
    
    private String appId;
    private String appSecret;
    private String apiUrl = "https://developer.toutiao.com";
    private String callbackUrl;
    
    private final Map<Long, String> shopTokens = new ConcurrentHashMap<>();
    
    public String getAccessToken(Long shopId) {
        return shopTokens.get(shopId);
    }
    
    public void setAccessToken(Long shopId, String token) {
        shopTokens.put(shopId, token);
    }
}