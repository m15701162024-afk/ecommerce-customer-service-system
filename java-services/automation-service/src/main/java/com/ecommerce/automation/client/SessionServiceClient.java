package com.ecommerce.automation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 会话服务Feign客户端
 * 用于调用会话服务的接口进行会话转接等操作
 */
@FeignClient(name = "session-service", path = "/api/sessions")
public interface SessionServiceClient {

    /**
     * 转接会话
     *
     * @param sessionId 会话ID
     * @param shopId    店铺ID
     * @param request   转接请求体
     * @return 转接结果
     */
    @PostMapping("/{sessionId}/transfer")
    Map<String, Object> transferSession(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("shopId") Long shopId,
            @RequestBody TransferRequest request
    );

    /**
     * 转接请求体
     */
    record TransferRequest(
            String agentGroup,
            String reason,
            Long targetAgentId,
            Map<String, Object> metadata
    ) {
        public TransferRequest(String agentGroup, String reason) {
            this(agentGroup, reason, null, null);
        }
    }
}