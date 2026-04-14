package com.ecommerce.platform.a1688.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token响应DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 会员ID
     */
    private String memberId;
    
    /**
     * 会员名称
     */
    private String memberName;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 站点信息
     */
    private String site;
    
    /**
     * 错误码
     */
    private Integer errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return errorCode == null || errorCode == 0;
    }
}