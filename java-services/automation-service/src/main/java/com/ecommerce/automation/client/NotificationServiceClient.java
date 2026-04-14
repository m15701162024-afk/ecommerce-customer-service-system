package com.ecommerce.automation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 通知服务Feign客户端
 * 用于调用通知服务发送各类通知
 */
@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationServiceClient {

    /**
     * 发送通知
     *
     * @param shopId  店铺ID
     * @param userId  用户ID
     * @param request 通知请求体
     * @return 发送结果
     */
    @PostMapping("/send")
    Map<String, Object> sendNotification(
            @RequestParam("shopId") Long shopId,
            @RequestParam("userId") Long userId,
            @RequestBody NotificationRequest request
    );

    /**
     * 通知请求体
     */
    record NotificationRequest(
            String type,
            String title,
            String content,
            Map<String, Object> extra
    ) {
        public NotificationRequest(String type, String content) {
            this(type, null, content, null);
        }
    }
}