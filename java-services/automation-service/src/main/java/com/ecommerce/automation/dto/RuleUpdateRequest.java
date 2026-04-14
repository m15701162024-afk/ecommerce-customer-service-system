package com.ecommerce.automation.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则更新请求
 */
@Data
public class RuleUpdateRequest {

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 触发条件配置
     */
    private Map<String, Object> triggerConfig;

    /**
     * 执行动作配置
     */
    private Map<String, Object> actionConfig;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 描述
     */
    private String description;

    /**
     * 开始时间
     */
    private LocalDateTime effectiveFrom;

    /**
     * 结束时间
     */
    private LocalDateTime effectiveTo;
}