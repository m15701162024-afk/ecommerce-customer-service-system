package com.ecommerce.automation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 消息服务Feign客户端
 * 用于调用消息服务的接口发送自动回复消息
 */
@FeignClient(name = "message-service", path = "/api/messages")
public interface MessageServiceClient {

    /**
     * 发送自动回复消息
     *
     * @param sessionId 会话ID
     * @param shopId    店铺ID
     * @param userId    用户ID
     * @param request   消息请求体
     * @return 发送结果
     */
    @PostMapping("/auto-reply")
    Map<String, Object> sendAutoReply(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("shopId") Long shopId,
            @RequestParam("userId") Long userId,
            @RequestBody MessageRequest request
    );

    /**
     * 消息请求体
     */
    record MessageRequest(
            String content,
            String messageType,
            Map<String, Object> metadata
    ) {
        public MessageRequest(String content) {
            this(content, "TEXT", null);
        }
    }
}