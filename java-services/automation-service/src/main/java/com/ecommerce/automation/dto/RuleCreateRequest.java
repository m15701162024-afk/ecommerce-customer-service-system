package com.ecommerce.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则创建请求
 */
@Data
public class RuleCreateRequest {

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String name;

    /**
     * 规则类型: KEYWORD, TIME, EVENT
     */
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /**
     * 店铺ID (null表示全局规则)
     */
    private Long shopId;

    /**
     * 触发条件配置
     */
    @NotNull(message = "触发条件配置不能为空")
    private Map<String, Object> triggerConfig;

    /**
     * 执行动作配置
     */
    @NotNull(message = "执行动作配置不能为空")
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