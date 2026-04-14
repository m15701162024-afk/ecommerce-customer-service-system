package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;
import java.util.List;

/**
 * 小红书OAuth2.0 Token响应
 */
@Data
public class TokenResponse {
    /**
     * 错误码，0表示成功
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private Data data;
    
    @Data
    public static class Data {
        /**
         * 访问令牌
         */
        private String accessToken;
        
        /**
         * 刷新令牌
         */
        private String refreshToken;
        
        /**
         * 过期时间(秒)
         */
        private Long expiresIn;
        
        /**
         * 刷新令牌过期时间(秒)
         */
        private Long refreshExpiresIn;
        
        /**
         * 授权范围
         */
        private String scope;
        
        /**
         * 用户ID
         */
        private String userId;
        
        /**
         * 开放平台用户ID
         */
        private String openId;
    }
}