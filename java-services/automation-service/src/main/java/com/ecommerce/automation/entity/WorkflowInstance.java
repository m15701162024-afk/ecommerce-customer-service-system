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
 * 工作流实例实体
 * 跟踪工作流执行状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_workflow_instance", autoResultMap = true)
public class WorkflowInstance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工作流定义ID
     */
    private Long workflowDefinitionId;

    /**
     * 工作流定义版本
     */
    private Integer definitionVersion;

    /**
     * 会话ID (关联客服会话)
     */
    private Long sessionId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 业务ID (如订单ID)
     */
    private String businessId;

    /**
     * 业务类型 (如ORDER, REFUND)
     */
    private String businessType;

    /**
     * 当前步骤ID
     */
    private String currentStepId;

    /**
     * 执行状态: RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
     */
    private String status;

    /**
     * 上下文变量
     * 存储工作流执行过程中的变量值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> context;

    /**
     * 执行结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime finishedAt;

    /**
     * 超时时间
     */
    private LocalDateTime timeoutAt;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 触发来源
     */
    private String triggerSource;
}