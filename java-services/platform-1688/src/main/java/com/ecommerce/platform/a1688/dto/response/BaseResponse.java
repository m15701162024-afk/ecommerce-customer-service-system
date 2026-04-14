package com.ecommerce.platform.a1688.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础响应DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {
    
    /**
     * 错误码
     * 0: 成功
     * 其他: 失败
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
        return errorCode != null && errorCode == 0;
    }
}