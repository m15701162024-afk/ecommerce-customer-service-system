package com.ecommerce.platform.a1688.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息发送请求DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    
    /**
     * 接收消息的会员ID（供应商）
     */
    @NotBlank(message = "接收者会员ID不能为空")
    private String toMemberId;
    
    /**
     * 消息类型
     * text: 文本消息
     * image: 图片消息
     * product: 商品卡片
     * order: 订单卡片
     */
    @NotBlank(message = "消息类型不能为空")
    @Builder.Default
    private String messageType = "text";
    
    /**
     * 消息内容（文本消息为文本内容，图片消息为图片URL）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容长度不能超过2000字符")
    private String content;
    
    /**
     * 关联商品ID（可选）
     */
    private String productId;
    
    /**
     * 关联订单ID（可选）
     */
    private String orderId;
    
    /**
     * 客户端消息ID（用于幂等性）
     */
    private String clientMsgId;
}