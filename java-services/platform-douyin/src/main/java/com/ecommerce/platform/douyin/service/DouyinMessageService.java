package com.ecommerce.platform.douyin.service;

import com.ecommerce.platform.douyin.client.DouyinApiClient;
import com.ecommerce.platform.douyin.config.DouyinConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DouyinMessageService {
    
    private final DouyinApiClient apiClient;
    private final DouyinConfig config;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CHAT_MESSAGE_TOPIC = "ecommerce-chat-messages";
    
    public void handleChatMessage(Map<String, Object> payload) {
        log.info("处理抖音聊天消息: {}", payload);
        
        Map<String, Object> content = (Map<String, Object>) payload.get("content");
        if (content == null) {
            return;
        }
        
        ChatMessageEvent event = new ChatMessageEvent();
        event.setPlatform("DOUYIN");
        event.setFromUserId((String) payload.get("from_user_id"));
        event.setToUserId((String) payload.get("to_user_id"));
        event.setConversationId((String) content.get("conversation_short_id"));
        event.setMessageId((String) content.get("server_message_id"));
        event.setMessageType((String) content.get("message_type"));
        event.setContent((String) content.get("text"));
        event.setCreateTime((Long) content.get("create_time"));
        
        kafkaTemplate.send(CHAT_MESSAGE_TOPIC, event.getConversationId(), event);
        
        log.info("聊天消息已发送到Kafka: conversationId={}", event.getConversationId());
    }
    
    public void handleOrderStatusChange(Map<String, Object> payload) {
        log.info("处理抖音订单状态变更: {}", payload);
        
        String orderId = (String) payload.get("order_id");
        Integer newStatus = (Integer) payload.get("new_status");
        
        OrderStatusEvent event = new OrderStatusEvent();
        event.setPlatform("DOUYIN");
        event.setOrderId(orderId);
        event.setNewStatus(newStatus);
        
        kafkaTemplate.send("ecommerce-order-status", orderId, event);
    }
    
    public void handleAuthCode(String code, String state) {
        log.info("处理抖音授权码: code={}, state={}", code, state);
        
        var response = apiClient.getAccessToken(code);
        
        if (response != null && response.getData() != null) {
            Long shopId = Long.parseLong(state);
            config.setAccessToken(shopId, response.getData().getAccessToken());
            
            log.info("抖音授权成功: shopId={}", shopId);
        }
    }
    
    public boolean sendMessage(Long shopId, String openId, String content) {
        return apiClient.sendMessage(shopId, openId, content, "text");
    }
    
    @lombok.Data
    public static class ChatMessageEvent {
        private String platform;
        private String fromUserId;
        private String toUserId;
        private String conversationId;
        private String messageId;
        private String messageType;
        private String content;
        private Long createTime;
    }
    
    @lombok.Data
    public static class OrderStatusEvent {
        private String platform;
        private String orderId;
        private Integer newStatus;
    }
}