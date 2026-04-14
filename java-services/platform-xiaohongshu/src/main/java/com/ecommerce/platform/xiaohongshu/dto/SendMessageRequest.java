package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;

/**
 * 小红书消息发送请求
 */
@Data
public class SendMessageRequest {
    /**
     * 店铺ID
     */
    private Long shopId;
    
    /**
     * 接收用户ID
     */
    private String toUserId;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 消息类型: text, image, card, product
     */
    private String msgType;
    
    /**
     * 消息内容
     */
    private Object content;
    
    /**
     * 文本消息内容
     */
    @Data
    public static class TextContent {
        private String text;
    }
    
    /**
     * 图片消息内容
     */
    @Data
    public static class ImageContent {
        private String imageUrl;
        private String mediaId;
    }
    
    /**
     * 商品卡片消息内容
     */
    @Data
    public static class ProductCardContent {
        private String productId;
        private String productName;
        private String productImage;
        private Long price;
        private String productUrl;
    }
}