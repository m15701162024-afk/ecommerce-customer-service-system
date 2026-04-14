package com.ecommerce.platform.a1688.service;

import com.ecommerce.platform.a1688.client.A1688ApiClient;
import com.ecommerce.platform.a1688.config.A1688Config;
import com.ecommerce.platform.a1688.dto.request.MessageRequest;
import com.ecommerce.platform.a1688.dto.response.BaseResponse;
import com.ecommerce.platform.a1688.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 1688平台主服务
 * 
 * 功能：
 * - OAuth2.0授权管理
 * - Token刷新与维护
 * - 消息处理
 * - 事件发布
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class A1688Service {
    
    private final A1688ApiClient apiClient;
    private final A1688Config config;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CHAT_MESSAGE_TOPIC = "ecommerce-chat-messages";
    private static final String ORDER_STATUS_TOPIC = "ecommerce-order-status";
    private static final String AUTH_EVENT_TOPIC = "ecommerce-auth-events";
    
    // ==================== 授权管理 ====================
    
    /**
     * 生成授权URL
     * 
     * @param shopId 店铺ID
     * @return 授权URL
     */
    public String generateAuthUrl(Long shopId) {
        String state = String.valueOf(shopId);
        String authUrl = apiClient.generateAuthUrl(state, null);
        
        log.info("生成1688授权URL: shopId={}, url={}", shopId, authUrl);
        return authUrl;
    }
    
    /**
     * 处理授权回调
     * 
     * @param code 授权码
     * @param state 状态参数（店铺ID）
     */
    public void handleAuthCallback(String code, String state) {
        log.info("处理1688授权回调: code={}, state={}", code, state);
        
        try {
            Long shopId = Long.parseLong(state);
            
            TokenResponse tokenResponse = apiClient.getAccessToken(code);
            
            if (tokenResponse != null && tokenResponse.isSuccess()) {
                // 保存Token信息
                A1688Config.AccessTokenInfo tokenInfo = new A1688Config.AccessTokenInfo();
                tokenInfo.setAccessToken(tokenResponse.getAccessToken());
                tokenInfo.setRefreshToken(tokenResponse.getRefreshToken());
                tokenInfo.setExpiresIn(tokenResponse.getExpiresIn());
                tokenInfo.setCreateTime(System.currentTimeMillis());
                tokenInfo.setMemberId(tokenResponse.getMemberId());
                
                config.setAccessToken(shopId, tokenInfo);
                
                // 发布授权成功事件
                AuthEvent event = new AuthEvent();
                event.setShopId(shopId);
                event.setMemberId(tokenResponse.getMemberId());
                event.setEventType("AUTH_SUCCESS");
                event.setTimestamp(System.currentTimeMillis());
                
                kafkaTemplate.send(AUTH_EVENT_TOPIC, String.valueOf(shopId), event);
                
                log.info("1688授权成功: shopId={}, memberId={}", shopId, tokenResponse.getMemberId());
            } else {
                log.error("1688授权失败: code={}, error={}", 
                        tokenResponse != null ? tokenResponse.getErrorCode() : "null",
                        tokenResponse != null ? tokenResponse.getErrorMessage() : "null");
                
                // 发布授权失败事件
                AuthEvent event = new AuthEvent();
                event.setShopId(shopId);
                event.setEventType("AUTH_FAILED");
                event.setErrorMessage(tokenResponse != null ? tokenResponse.getErrorMessage() : "未知错误");
                event.setTimestamp(System.currentTimeMillis());
                
                kafkaTemplate.send(AUTH_EVENT_TOPIC, String.valueOf(shopId), event);
            }
        } catch (Exception e) {
            log.error("处理1688授权回调异常", e);
            throw new RuntimeException("处理授权回调失败", e);
        }
    }
    
    /**
     * 刷新Token
     * 
     * @param shopId 店铺ID
     * @return 是否成功
     */
    public boolean refreshToken(Long shopId) {
        log.info("刷新1688 Token: shopId={}", shopId);
        
        A1688Config.AccessTokenInfo tokenInfo = config.getAccessToken(shopId);
        
        if (tokenInfo == null) {
            log.warn("店铺Token不存在: shopId={}", shopId);
            return false;
        }
        
        try {
            TokenResponse tokenResponse = apiClient.refreshAccessToken(tokenInfo.getRefreshToken());
            
            if (tokenResponse != null && tokenResponse.isSuccess()) {
                // 更新Token信息
                tokenInfo.setAccessToken(tokenResponse.getAccessToken());
                tokenInfo.setRefreshToken(tokenResponse.getRefreshToken());
                tokenInfo.setExpiresIn(tokenResponse.getExpiresIn());
                tokenInfo.setCreateTime(System.currentTimeMillis());
                
                config.setAccessToken(shopId, tokenInfo);
                
                log.info("Token刷新成功: shopId={}", shopId);
                return true;
            } else {
                log.error("Token刷新失败: shopId={}, error={}", 
                        shopId,
                        tokenResponse != null ? tokenResponse.getErrorMessage() : "null");
                
                // 移除无效的Token
                config.removeAccessToken(shopId);
                return false;
            }
        } catch (Exception e) {
            log.error("刷新Token异常: shopId={}", shopId, e);
            return false;
        }
    }
    
    /**
     * 检查并刷新即将过期的Token
     * 定时任务：每小时执行一次
     */
    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    public void checkAndRefreshTokens() {
        log.info("开始检查Token过期状态...");
        
        config.getShopTokens().forEach((shopId, tokenInfo) -> {
            // 提前30分钟刷新
            if (tokenInfo.isExpired() || isTokenExpiringSoon(tokenInfo, 30)) {
                log.info("Token即将过期，开始刷新: shopId={}", shopId);
                refreshToken(shopId);
            }
        });
    }
    
    /**
     * 检查Token是否即将过期
     * 
     * @param tokenInfo Token信息
     * @param minutes 提前分钟数
     * @return 是否即将过期
     */
    private boolean isTokenExpiringSoon(A1688Config.AccessTokenInfo tokenInfo, int minutes) {
        if (tokenInfo.getCreateTime() == null || tokenInfo.getExpiresIn() == null) {
            return true;
        }
        
        long expireTime = tokenInfo.getCreateTime() + tokenInfo.getExpiresIn() * 1000;
        long warningTime = expireTime - minutes * 60 * 1000;
        
        return System.currentTimeMillis() > warningTime;
    }
    
    // ==================== 消息处理 ====================
    
    /**
     * 处理聊天消息回调
     * 
     * @param payload 回调数据
     */
    public void handleChatMessage(Map<String, Object> payload) {
        log.info("处理1688聊天消息: {}", payload);
        
        try {
            Map<String, Object> content = (Map<String, Object>) payload.get("content");
            if (content == null) {
                return;
            }
            
            ChatMessageEvent event = new ChatMessageEvent();
            event.setPlatform("A1688");
            event.setFromMemberId((String) payload.get("from_member_id"));
            event.setToMemberId((String) payload.get("to_member_id"));
            event.setConversationId((String) payload.get("conversation_id"));
            event.setMessageId((String) payload.get("message_id"));
            event.setMessageType((String) content.get("message_type"));
            event.setContent((String) content.get("text"));
            event.setCreateTime((Long) payload.get("create_time"));
            
            kafkaTemplate.send(CHAT_MESSAGE_TOPIC, event.getConversationId(), event);
            
            log.info("聊天消息已发送到Kafka: conversationId={}", event.getConversationId());
        } catch (Exception e) {
            log.error("处理聊天消息异常", e);
        }
    }
    
    /**
     * 处理订单状态变更回调
     * 
     * @param payload 回调数据
     */
    public void handleOrderStatusChange(Map<String, Object> payload) {
        log.info("处理1688订单状态变更: {}", payload);
        
        try {
            String orderId = (String) payload.get("order_id");
            String newStatus = (String) payload.get("new_status");
            Long shopId = ((Number) payload.get("shop_id")).longValue();
            
            OrderStatusEvent event = new OrderStatusEvent();
            event.setPlatform("A1688");
            event.setShopId(shopId);
            event.setOrderId(orderId);
            event.setNewStatus(newStatus);
            event.setTimestamp(System.currentTimeMillis());
            
            kafkaTemplate.send(ORDER_STATUS_TOPIC, orderId, event);
            
            log.info("订单状态变更已发送到Kafka: orderId={}, status={}", orderId, newStatus);
        } catch (Exception e) {
            log.error("处理订单状态变更异常", e);
        }
    }
    
    /**
     * 发送消息给供应商
     * 
     * @param shopId 店铺ID
     * @param toMemberId 供应商会员ID
     * @param content 消息内容
     * @return 是否成功
     */
    public boolean sendMessage(Long shopId, String toMemberId, String content) {
        MessageRequest request = MessageRequest.builder()
                .toMemberId(toMemberId)
                .messageType("text")
                .content(content)
                .build();
        
        BaseResponse response = apiClient.sendMessage(shopId, request);
        
        return response != null && response.isSuccess();
    }
    
    // ==================== 事件定义 ====================
    
    @lombok.Data
    public static class ChatMessageEvent {
        private String platform;
        private String fromMemberId;
        private String toMemberId;
        private String conversationId;
        private String messageId;
        private String messageType;
        private String content;
        private Long createTime;
    }
    
    @lombok.Data
    public static class OrderStatusEvent {
        private String platform;
        private Long shopId;
        private String orderId;
        private String newStatus;
        private Long timestamp;
    }
    
    @lombok.Data
    public static class AuthEvent {
        private Long shopId;
        private String memberId;
        private String eventType;
        private String errorMessage;
        private Long timestamp;
    }
}