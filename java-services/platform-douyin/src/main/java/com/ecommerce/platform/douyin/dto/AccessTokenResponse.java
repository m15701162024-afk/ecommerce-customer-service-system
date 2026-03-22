package com.ecommerce.platform.douyin.dto;

import lombok.Data;
import java.util.List;

@Data
public class AccessTokenResponse {
    private Integer errNo;
    private String errMsg;
    private Data data;
    
    @lombok.Data
    public static class Data {
        private String accessToken;
        private String refreshToken;
        private Integer expiresIn;
        private String openId;
    }
}