package com.ecommerce.automation.executor;

import com.ecommerce.automation.client.MessageServiceClient;
import com.ecommerce.automation.client.MessageServiceClient.MessageRequest;
import com.ecommerce.automation.client.NotificationServiceClient;
import com.ecommerce.automation.client.NotificationServiceClient.NotificationRequest;
import com.ecommerce.automation.client.SessionServiceClient;
import com.ecommerce.automation.client.SessionServiceClient.TransferRequest;
import com.ecommerce.automation.entity.AutoRule;
import com.ecommerce.automation.service.AutoRuleService;
import com.ecommerce.automation.service.TriggerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则执行器
 * 负责执行自动化规则的动作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleExecutor {

    private final AutoRuleService autoRuleService;
    private final TriggerService triggerService;
    private final ObjectMapper objectMapper;
    private final MessageServiceClient messageServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final SessionServiceClient sessionServiceClient;
    private final RestTemplate restTemplate;

    /**
     * 执行规则
     *
     * @param rule          规则实体
     * @param sessionId     会话ID
     * @param shopId        店铺ID
     * @param userId        用户ID
     * @param triggerType   触发类型
     * @param triggerContent 触发内容
     */
    public void executeRule(AutoRule rule, Long sessionId, Long shopId, Long userId, 
                           String triggerType, String triggerContent) {
        long startTime = System.currentTimeMillis();
        String status = "SUCCESS";
        String resultMessage = null;
        String errorMessage = null;
        
        try {
            log.info("开始执行规则, ruleId={}, ruleName={}", rule.getId(), rule.getName());
            
            // 检查规则有效期
            if (!isRuleEffective(rule)) {
                log.info("规则不在有效期内, ruleId={}", rule.getId());
                return;
            }
            
            Map<String, Object> actionConfig = rule.getActionConfig();
            String actionType = (String) actionConfig.get("actionType");
            
            // 根据动作类型执行相应操作
            switch (actionType) {
                case "REPLY" -> executeReplyAction(actionConfig, sessionId, shopId, userId);
                case "NOTIFY" -> executeNotifyAction(actionConfig, shopId, userId);
                case "TRANSFER" -> executeTransferAction(actionConfig, sessionId, shopId);
                case "WEBHOOK" -> executeWebhookAction(actionConfig, rule, triggerContent);
                default -> log.warn("未知的动作类型: {}", actionType);
            }
            
            // 增加执行次数
            autoRuleService.incrementExecutionCount(rule.getId());
            
            resultMessage = "执行成功";
            log.info("规则执行成功, ruleId={}, duration={}ms", rule.getId(), 
                     System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            status = "FAILED";
            errorMessage = e.getMessage();
            resultMessage = "执行失败: " + e.getMessage();
            log.error("规则执行失败, ruleId={}, error={}", rule.getId(), e.getMessage(), e);
        } finally {
            // 记录执行日志
            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> actionConfig = rule.getActionConfig();
            String actionType = actionConfig != null ? (String) actionConfig.get("actionType") : "UNKNOWN";
            String actionDetail = actionConfig != null ? toJsonString(actionConfig) : null;
            
            triggerService.logExecution(
                "RULE", rule.getId(), sessionId, shopId, userId,
                triggerType, triggerContent, actionType, actionDetail,
                status, resultMessage, duration
            );
        }
    }

    /**
     * 执行自动回复动作
     */
    private void executeReplyAction(Map<String, Object> actionConfig, Long sessionId, 
                                    Long shopId, Long userId) {
        String content = (String) actionConfig.get("content");
        String template = (String) actionConfig.get("template");
        
        String message;
        if (template != null) {
            // 使用模板生成消息
            message = processTemplate(template, actionConfig);
        } else {
            message = content;
        }
        
        log.info("执行自动回复: sessionId={}, message={}", sessionId, message);
        
        try {
            MessageRequest request = new MessageRequest(message, "TEXT", null);
            messageServiceClient.sendAutoReply(sessionId, shopId, userId, request);
            log.debug("自动回复发送成功: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("自动回复发送失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new RuntimeException("自动回复发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行通知动作
     */
    private void executeNotifyAction(Map<String, Object> actionConfig, Long shopId, Long userId) {
        String content = (String) actionConfig.get("content");
        String notifyType = (String) actionConfig.getOrDefault("notifyType", "SYSTEM");
        String title = (String) actionConfig.get("title");
        
        log.info("发送通知: shopId={}, userId={}, type={}, content={}", shopId, userId, notifyType, content);
        
        try {
            NotificationRequest request = new NotificationRequest(notifyType, title, content, null);
            notificationServiceClient.sendNotification(shopId, userId, request);
            log.debug("通知发送成功: shopId={}, userId={}", shopId, userId);
        } catch (Exception e) {
            log.error("通知发送失败: shopId={}, userId={}, error={}", shopId, userId, e.getMessage(), e);
            throw new RuntimeException("通知发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行转接动作
     */
    private void executeTransferAction(Map<String, Object> actionConfig, Long sessionId, Long shopId) {
        String agentGroup = (String) actionConfig.get("agentGroup");
        String transferReason = (String) actionConfig.get("reason");
        Long targetAgentId = actionConfig.get("targetAgentId") != null 
            ? ((Number) actionConfig.get("targetAgentId")).longValue() 
            : null;
        
        log.info("执行会话转接: sessionId={}, agentGroup={}, reason={}", sessionId, agentGroup, transferReason);
        
        try {
            TransferRequest request = new TransferRequest(agentGroup, transferReason, targetAgentId, null);
            sessionServiceClient.transferSession(sessionId, shopId, request);
            log.debug("会话转接成功: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("会话转接失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new RuntimeException("会话转接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行Webhook动作
     */
    private void executeWebhookAction(Map<String, Object> actionConfig, AutoRule rule, 
                                      String triggerContent) {
        String url = (String) actionConfig.get("url");
        String method = (String) actionConfig.getOrDefault("method", "POST");
        Integer timeout = actionConfig.get("timeout") != null 
            ? ((Number) actionConfig.get("timeout")).intValue() 
            : 5000;
        
        log.info("执行Webhook: url={}, method={}, ruleId={}", url, method, rule.getId());
        
        if (url == null || url.isBlank()) {
            log.warn("Webhook URL为空，跳过执行: ruleId={}", rule.getId());
            return;
        }
        
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ruleId", rule.getId());
            requestBody.put("ruleName", rule.getName());
            requestBody.put("triggerContent", triggerContent);
            requestBody.put("timestamp", System.currentTimeMillis());
            
            // 添加自定义payload
            @SuppressWarnings("unchecked")
            Map<String, Object> customPayload = (Map<String, Object>) actionConfig.get("payload");
            if (customPayload != null) {
                requestBody.putAll(customPayload);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 添加自定义headers
            @SuppressWarnings("unchecked")
            Map<String, String> customHeaders = (Map<String, String>) actionConfig.get("headers");
            if (customHeaders != null) {
                customHeaders.forEach(headers::add);
            }
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
            
            log.info("Webhook调用成功: url={}, statusCode={}, response={}", 
                     url, response.getStatusCode(), response.getBody());
            
        } catch (Exception e) {
            log.error("Webhook调用失败: url={}, ruleId={}, error={}", url, rule.getId(), e.getMessage(), e);
            throw new RuntimeException("Webhook调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查规则是否在有效期内
     */
    private boolean isRuleEffective(AutoRule rule) {
        LocalDateTime now = LocalDateTime.now();
        
        if (rule.getEffectiveFrom() != null && now.isBefore(rule.getEffectiveFrom())) {
            return false;
        }
        
        if (rule.getEffectiveTo() != null && now.isAfter(rule.getEffectiveTo())) {
            return false;
        }
        
        return true;
    }

    /**
     * 处理模板变量
     */
    private String processTemplate(String template, Map<String, Object> variables) {
        if (variables == null) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        
        return result;
    }

    /**
     * 对象转JSON字符串
     */
    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}