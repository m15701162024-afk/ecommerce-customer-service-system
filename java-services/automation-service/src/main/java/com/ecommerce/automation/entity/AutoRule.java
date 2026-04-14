package com.ecommerce.automation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 自动化规则实体
 * 支持关键词触发、定时触发、事件触发
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auto_rule", autoResultMap = true)
public class AutoRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则类型: KEYWORD, TIME, EVENT
     */
    private String ruleType;

    /**
     * 店铺ID (null表示全局规则)
     */
    private Long shopId;

    /**
     * 触发条件配置 (JSON格式)
     * KEYWORD: {"keywords": ["关键词1", "关键词2"], "matchType": "EXACT|FUZZY"}
     * TIME: {"cron": "0 0 9 * * ?", "timezone": "Asia/Shanghai"}
     * EVENT: {"eventType": "ORDER_CREATED|PAYMENT_SUCCESS|ORDER_SHIPPED"}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> triggerConfig;

    /**
     * 执行动作配置 (JSON格式)
     * {"actionType": "REPLY|NOTIFY|TRANSFER|WEBHOOK", "content": "...", "template": "..."}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> actionConfig;

    /**
     * 优先级 (数值越小优先级越高)
     */
    private Integer priority;

    /**
     * 规则状态: ENABLED, DISABLED
     */
    private String status;

    /**
     * 开始时间 (可选)
     */
    private LocalDateTime effectiveFrom;

    /**
     * 结束时间 (可选)
     */
    private LocalDateTime effectiveTo;

    /**
     * 描述
     */
    private String description;

    /**
     * 执行次数统计
     */
    private Long executionCount;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecutedAt;
}