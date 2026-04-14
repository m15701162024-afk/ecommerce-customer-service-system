package com.ecommerce.automation.service;

import com.ecommerce.automation.entity.AutoRule;
import com.ecommerce.automation.entity.ExecutionLog;
import com.ecommerce.automation.executor.RuleExecutor;
import com.ecommerce.automation.mapper.AutoRuleMapper;
import com.ecommerce.automation.mapper.ExecutionLogMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 触发器服务
 * 负责检测和处理各种触发事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerService {

    private final AutoRuleMapper autoRuleMapper;
    private final ExecutionLogMapper executionLogMapper;
    private final RuleExecutor ruleExecutor;
    private final AutoRuleService autoRuleService;
    private final ObjectMapper objectMapper;

    // ==================== 关键词触发 ====================

    /**
     * 处理消息关键词触发
     * 当收到客服消息时调用此方法
     *
     * @param sessionId 会话ID
     * @param shopId    店铺ID
     * @param userId    用户ID
     * @param message   消息内容
     * @return 是否触发了规则
     */
    public boolean processKeywordTrigger(Long sessionId, Long shopId, Long userId, String message) {
        log.debug("处理关键词触发, sessionId={}, message={}", sessionId, message);
        
        // 获取启用的关键词规则
        List<AutoRule> keywordRules = autoRuleMapper.findEnabledRulesByType("KEYWORD");
        
        // 过滤出适用于当前店铺的规则
        List<AutoRule> applicableRules = keywordRules.stream()
            .filter(rule -> rule.getShopId() == null || rule.getShopId().equals(shopId))
            .toList();
        
        for (AutoRule rule : applicableRules) {
            Map<String, Object> triggerConfig = rule.getTriggerConfig();
            List<String> keywords = (List<String>) triggerConfig.get("keywords");
            String matchType = (String) triggerConfig.getOrDefault("matchType", "FUZZY");
            
            boolean matched = false;
            String matchedKeyword = null;
            
            for (String keyword : keywords) {
                if ("EXACT".equals(matchType)) {
                    if (message.equals(keyword)) {
                        matched = true;
                        matchedKeyword = keyword;
                        break;
                    }
                } else {
                    // FUZZY - 模糊匹配
                    if (message.contains(keyword)) {
                        matched = true;
                        matchedKeyword = keyword;
                        break;
                    }
                }
            }
            
            if (matched) {
                log.info("关键词规则匹配成功, ruleId={}, ruleName={}, keyword={}", 
                         rule.getId(), rule.getName(), matchedKeyword);
                
                // 执行规则
                ruleExecutor.executeRule(rule, sessionId, shopId, userId, "KEYWORD", matchedKeyword);
                
                // 返回true表示触发了规则（只触发第一个匹配的规则）
                return true;
            }
        }
        
        return false;
    }

    // ==================== 事件触发 ====================

    /**
     * 处理订单创建事件
     */
    @KafkaListener(topics = "order.created", groupId = "automation-service")
    public void handleOrderCreatedEvent(String eventJson) {
        log.info("收到订单创建事件: {}", eventJson);
        processEventTrigger("ORDER_CREATED", eventJson);
    }

    /**
     * 处理支付成功事件
     */
    @KafkaListener(topics = "order.paid", groupId = "automation-service")
    public void handlePaymentSuccessEvent(String eventJson) {
        log.info("收到支付成功事件: {}", eventJson);
        processEventTrigger("PAYMENT_SUCCESS", eventJson);
    }

    /**
     * 处理订单发货事件
     */
    @KafkaListener(topics = "order.shipped", groupId = "automation-service")
    public void handleOrderShippedEvent(String eventJson) {
        log.info("收到订单发货事件: {}", eventJson);
        processEventTrigger("ORDER_SHIPPED", eventJson);
    }

    /**
     * 处理订单完成事件
     */
    @KafkaListener(topics = "order.completed", groupId = "automation-service")
    public void handleOrderCompletedEvent(String eventJson) {
        log.info("收到订单完成事件: {}", eventJson);
        processEventTrigger("ORDER_COMPLETED", eventJson);
    }

    /**
     * 处理用户首次咨询事件 (欢迎语触发)
     */
    public void handleFirstConsultation(Long sessionId, Long shopId, Long userId) {
        log.info("处理首次咨询事件, sessionId={}, shopId={}, userId={}", sessionId, shopId, userId);
        
        // 获取启用的欢迎语规则
        List<AutoRule> eventRules = autoRuleMapper.findEnabledRulesByType("EVENT");
        
        eventRules.stream()
            .filter(rule -> rule.getShopId() == null || rule.getShopId().equals(shopId))
            .filter(rule -> "FIRST_CONSULTATION".equals(
                rule.getTriggerConfig().get("eventType")))
            .forEach(rule -> {
                log.info("欢迎语规则触发, ruleId={}, ruleName={}", rule.getId(), rule.getName());
                ruleExecutor.executeRule(rule, sessionId, shopId, userId, "EVENT", "FIRST_CONSULTATION");
            });
    }

    /**
     * 处理事件触发
     */
    private void processEventTrigger(String eventType, String eventData) {
        List<AutoRule> eventRules = autoRuleMapper.findEnabledRulesByType("EVENT");
        
        // 解析事件数据获取shopId, userId等
        Long shopId = null;
        Long userId = null;
        Long sessionId = null;
        
        try {
            Map<String, Object> eventMap = objectMapper.readValue(eventData, 
                new TypeReference<Map<String, Object>>() {});
            
            // 提取通用字段
            if (eventMap.get("shopId") != null) {
                shopId = ((Number) eventMap.get("shopId")).longValue();
            }
            if (eventMap.get("userId") != null) {
                userId = ((Number) eventMap.get("userId")).longValue();
            }
            if (eventMap.get("sessionId") != null) {
                sessionId = ((Number) eventMap.get("sessionId")).longValue();
            }
            
            // 尝试从嵌套的data字段获取
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) eventMap.get("data");
            if (data != null) {
                if (shopId == null && data.get("shopId") != null) {
                    shopId = ((Number) data.get("shopId")).longValue();
                }
                if (userId == null && data.get("userId") != null) {
                    userId = ((Number) data.get("userId")).longValue();
                }
                if (sessionId == null && data.get("sessionId") != null) {
                    sessionId = ((Number) data.get("sessionId")).longValue();
                }
            }
            
            log.debug("解析事件数据: eventType={}, shopId={}, userId={}, sessionId={}", 
                     eventType, shopId, userId, sessionId);
            
        } catch (Exception e) {
            log.warn("解析事件数据失败: eventType={}, eventData={}, error={}", 
                    eventType, eventData, e.getMessage());
        }
        
        for (AutoRule rule : eventRules) {
            Map<String, Object> triggerConfig = rule.getTriggerConfig();
            String ruleEventType = (String) triggerConfig.get("eventType");
            
            if (eventType.equals(ruleEventType)) {
                log.info("事件规则触发, ruleId={}, ruleName={}, eventType={}", 
                         rule.getId(), rule.getName(), eventType);
                
                ruleExecutor.executeRule(rule, sessionId, shopId, userId, "EVENT", eventData);
            }
        }
    }

    // ==================== 定时触发 ====================

    /**
     * 定时检查并执行定时规则
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkScheduledRules() {
        log.debug("检查定时规则...");
        
        List<AutoRule> timeRules = autoRuleMapper.findEnabledRulesByType("TIME");
        LocalDateTime now = LocalDateTime.now();
        
        for (AutoRule rule : timeRules) {
            try {
                Map<String, Object> triggerConfig = rule.getTriggerConfig();
                String cronExpression = (String) triggerConfig.get("cron");
                
                if (cronExpression == null || cronExpression.isBlank()) {
                    log.warn("规则缺少cron表达式: ruleId={}", rule.getId());
                    continue;
                }
                
                // 检查规则有效期
                if (rule.getEffectiveFrom() != null && now.isBefore(rule.getEffectiveFrom())) {
                    continue;
                }
                if (rule.getEffectiveTo() != null && now.isAfter(rule.getEffectiveTo())) {
                    continue;
                }
                
                // 使用Spring的CronExpression进行匹配
                CronExpression cron = CronExpression.parse(cronExpression);
                LocalDateTime nextExecution = cron.next(now.minusMinutes(1));
                
                // 判断当前时间是否匹配cron表达式
                if (nextExecution != null && 
                    nextExecution.getYear() == now.getYear() &&
                    nextExecution.getMonthValue() == now.getMonthValue() &&
                    nextExecution.getDayOfMonth() == now.getDayOfMonth() &&
                    nextExecution.getHour() == now.getHour() &&
                    nextExecution.getMinute() == now.getMinute()) {
                    
                    log.info("定时规则触发, ruleId={}, ruleName={}, cron={}", 
                            rule.getId(), rule.getName(), cronExpression);
                    
                    // 获取店铺ID
                    Long shopId = rule.getShopId();
                    Long userId = null;
                    Long sessionId = null;
                    
                    // 从triggerConfig获取额外参数
                    if (triggerConfig.get("shopId") != null) {
                        shopId = ((Number) triggerConfig.get("shopId")).longValue();
                    }
                    
                    ruleExecutor.executeRule(rule, sessionId, shopId, userId, "TIME", cronExpression);
                }
                
            } catch (Exception e) {
                log.error("处理定时规则失败: ruleId={}, error={}", rule.getId(), e.getMessage(), e);
            }
        }
    }

    // ==================== 订单状态变更通知 ====================

    /**
     * 发送订单状态变更通知
     *
     * @param shopId     店铺ID
     * @param userId     用户ID
     * @param orderNo    订单号
     * @param oldStatus  旧状态
     * @param newStatus  新状态
     */
    public void sendOrderStatusNotification(Long shopId, Long userId, String orderNo, 
                                            String oldStatus, String newStatus) {
        log.info("发送订单状态变更通知, shopId={}, orderNo={}, {} -> {}", shopId, orderNo, oldStatus, newStatus);
        
        // 获取订单状态变更通知规则
        List<AutoRule> eventRules = autoRuleMapper.findEnabledRulesByType("EVENT");
        
        eventRules.stream()
            .filter(rule -> rule.getShopId() == null || rule.getShopId().equals(shopId))
            .filter(rule -> "ORDER_STATUS_CHANGE".equals(
                rule.getTriggerConfig().get("eventType")))
            .forEach(rule -> {
                // 构建通知内容
                String statusMessage = buildOrderStatusMessage(newStatus, orderNo);
                
                // 修改动作配置中的内容
                Map<String, Object> actionConfig = rule.getActionConfig();
                actionConfig.put("content", statusMessage);
                
                ruleExecutor.executeRule(rule, null, shopId, userId, "EVENT", 
                    String.format("订单%s状态变更: %s -> %s", orderNo, oldStatus, newStatus));
            });
    }

    /**
     * 构建订单状态消息
     */
    private String buildOrderStatusMessage(String status, String orderNo) {
        return switch (status) {
            case "PAID" -> String.format("您的订单 %s 已支付成功，我们会尽快为您发货~", orderNo);
            case "SHIPPED" -> String.format("您的订单 %s 已发货，请注意查收~", orderNo);
            case "DELIVERED" -> String.format("您的订单 %s 已送达，感谢您的购买！", orderNo);
            case "COMPLETED" -> String.format("您的订单 %s 已完成，期待您的下次光临！", orderNo);
            case "CANCELLED" -> String.format("您的订单 %s 已取消", orderNo);
            default -> String.format("您的订单 %s 状态已更新为: %s", orderNo, status);
        };
    }

    // ==================== 执行日志记录 ====================

    /**
     * 记录执行日志
     */
    public void logExecution(String logType, Long relatedId, Long sessionId, Long shopId, Long userId,
                            String triggerType, String triggerContent, String actionType, 
                            String actionDetail, String status, String resultMessage, Long duration) {
        ExecutionLog logEntry = new ExecutionLog();
        logEntry.setLogType(logType);
        logEntry.setRelatedId(relatedId);
        logEntry.setSessionId(sessionId);
        logEntry.setShopId(shopId);
        logEntry.setUserId(userId);
        logEntry.setTriggerType(triggerType);
        logEntry.setTriggerContent(triggerContent);
        logEntry.setActionType(actionType);
        logEntry.setActionDetail(actionDetail);
        logEntry.setStatus(status);
        logEntry.setResultMessage(resultMessage);
        logEntry.setDuration(duration);
        logEntry.setExecutedAt(LocalDateTime.now());
        logEntry.setCreatedAt(LocalDateTime.now());
        logEntry.setUpdatedAt(LocalDateTime.now());
        logEntry.setDeleted(0);
        
        executionLogMapper.insert(logEntry);
    }
}