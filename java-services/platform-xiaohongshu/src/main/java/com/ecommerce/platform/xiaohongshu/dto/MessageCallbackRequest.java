package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;
import java.util.Map;

/**
 * 小红书消息回调请求
 */
@Data
public class MessageCallbackRequest {
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 消息内容
     */
    private Map<String, Object> content;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 签名
     */
    private String signature;
    
    /**
     * 店铺ID
     */
    private Long shopId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 发送者ID
     */
    private String fromUserId;
    
    /**
     * 接收者ID
     */
    private String toUserId;
}