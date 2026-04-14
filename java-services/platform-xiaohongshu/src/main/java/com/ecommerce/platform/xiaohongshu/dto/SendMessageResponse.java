package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;

/**
 * 小红书消息发送响应
 */
@Data
public class SendMessageResponse {
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
         * 消息ID
         */
        private String messageId;
        
        /**
         * 发送时间
         */
        private Long sendTime;
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}