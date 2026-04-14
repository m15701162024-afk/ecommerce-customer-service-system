package com.ecommerce.platform.xiaohongshu.service;

import com.ecommerce.platform.xiaohongshu.client.XiaohongshuApiClient;
import com.ecommerce.platform.xiaohongshu.dto.SendMessageRequest;
import com.ecommerce.platform.xiaohongshu.dto.SendMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 小红书消息服务
 * 
 * 功能:
 * - 接收消息回调
 * - 发送消息
 * - 消息事件处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XiaohongshuMessageService {

    private final XiaohongshuApiClient apiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CHAT_MESSAGE_TOPIC = "ecommerce-chat-messages";

    /**
     * 处理聊天消息回调
     */
    public void handleChatMessage(Map<String, Object> payload) {
        log.info("处理小红书聊天消息: {}", payload);

        try {
            Long shopId = getLongValue(payload, "shop_id");
            String fromUserId = getStringValue(payload, "from_user_id");
            String toUserId = getStringValue(payload, "to_user_id");
            String conversationId = getStringValue(payload, "conversation_id");
            String messageId = getStringValue(payload, "message_id");

            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) payload.get("content");
            if (content == null) {
                log.warn("消息内容为空");
                return;
            }

            String msgType = getStringValue(content, "msg_type");
            String text = getStringValue(content, "text");
            Long createTime = getLongValue(content, "create_time");

            ChatMessageEvent event = new ChatMessageEvent();
            event.setPlatform("XIAOHONGSHU");
            event.setShopId(shopId);
            event.setFromUserId(fromUserId);
            event.setToUserId(toUserId);
            event.setConversationId(conversationId);
            event.setMessageId(messageId);
            event.setMessageType(msgType);
            event.setContent(text);
            event.setCreateTime(createTime);

            kafkaTemplate.send(CHAT_MESSAGE_TOPIC, conversationId, event);
            log.info("聊天消息已发送到Kafka: conversationId={}, messageId={}", conversationId, messageId);

        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
        }
    }

    /**
     * 处理订单状态变更回调
     */
    public void handleOrderStatusChange(Map<String, Object> payload) {
        log.info("处理小红书订单状态变更: {}", payload);

        try {
            Long shopId = getLongValue(payload, "shop_id");
            String orderId = getStringValue(payload, "order_id");
            Integer newStatus = getIntegerValue(payload, "new_status");

            OrderStatusEvent event = new OrderStatusEvent();
            event.setPlatform("XIAOHONGSHU");
            event.setShopId(shopId);
            event.setOrderId(orderId);
            event.setNewStatus(newStatus);
            event.setTimestamp(System.currentTimeMillis());

            kafkaTemplate.send("ecommerce-order-status", orderId, event);
            log.info("订单状态变更事件已发送: orderId={}, newStatus={}", orderId, newStatus);

        } catch (Exception e) {
            log.error("处理订单状态变更失败", e);
        }
    }

    /**
     * 发送文本消息
     */
    public boolean sendTextMessage(Long shopId, String toUserId, String conversationId, String text) {
        log.info("发送小红书文本消息: shopId={}, toUserId={}", shopId, toUserId);

        try {
            SendMessageResponse response = apiClient.sendTextMessage(shopId, toUserId, conversationId, text);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("发送文本消息失败", e);
            return false;
        }
    }

    /**
     * 发送图片消息
     */
    public boolean sendImageMessage(Long shopId, String toUserId, String conversationId, String imageUrl, String mediaId) {
        log.info("发送小红书图片消息: shopId={}, toUserId={}", shopId, toUserId);

        try {
            SendMessageRequest request = new SendMessageRequest();
            request.setShopId(shopId);
            request.setToUserId(toUserId);
            request.setConversationId(conversationId);
            request.setMsgType("image");

            SendMessageRequest.ImageContent content = new SendMessageRequest.ImageContent();
            content.setImageUrl(imageUrl);
            content.setMediaId(mediaId);
            request.setContent(content);

            SendMessageResponse response = apiClient.sendMessage(shopId, request);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("发送图片消息失败", e);
            return false;
        }
    }

    /**
     * 发送商品卡片消息
     */
    public boolean sendProductCardMessage(Long shopId, String toUserId, String conversationId,
            String productId, String productName, String productImage, Long price, String productUrl) {
        log.info("发送小红书商品卡片消息: shopId={}, toUserId={}, productId={}", shopId, toUserId, productId);

        try {
            SendMessageRequest request = new SendMessageRequest();
            request.setShopId(shopId);
            request.setToUserId(toUserId);
            request.setConversationId(conversationId);
            request.setMsgType("product");

            SendMessageRequest.ProductCardContent content = new SendMessageRequest.ProductCardContent();
            content.setProductId(productId);
            content.setProductName(productName);
            content.setProductImage(productImage);
            content.setPrice(price);
            content.setProductUrl(productUrl);
            request.setContent(content);

            SendMessageResponse response = apiClient.sendMessage(shopId, request);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("发送商品卡片消息失败", e);
            return false;
        }
    }

    /**
     * 发送自定义消息
     */
    public boolean sendMessage(Long shopId, SendMessageRequest request) {
        log.info("发送小红书自定义消息: shopId={}, toUserId={}, msgType={}", 
                shopId, request.getToUserId(), request.getMsgType());

        try {
            SendMessageResponse response = apiClient.sendMessage(shopId, request);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("发送自定义消息失败", e);
            return false;
        }
    }

    // ==================== 辅助方法 ====================

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 聊天消息事件
     */
    @lombok.Data
    public static class ChatMessageEvent {
        private String platform;
        private Long shopId;
        private String fromUserId;
        private String toUserId;
        private String conversationId;
        private String messageId;
        private String messageType;
        private String content;
        private Long createTime;
    }

    /**
     * 订单状态事件
     */
    @lombok.Data
    public static class OrderStatusEvent {
        private String platform;
        private Long shopId;
        private String orderId;
        private Integer newStatus;
        private Long timestamp;
    }
}