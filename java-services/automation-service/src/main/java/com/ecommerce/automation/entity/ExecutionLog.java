package com.ecommerce.automation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 执行日志实体
 * 记录自动化规则和工作流的执行历史
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_execution_log")
public class ExecutionLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 日志类型: RULE, WORKFLOW
     */
    private String logType;

    /**
     * 关联ID (规则ID或工作流实例ID)
     */
    private Long relatedId;

    /**
     * 会话ID
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
     * 触发类型: KEYWORD, TIME, EVENT, MANUAL
     */
    private String triggerType;

    /**
     * 触发内容 (如匹配的关键词、事件数据等)
     */
    private String triggerContent;

    /**
     * 执行动作类型
     */
    private String actionType;

    /**
     * 执行动作详情
     */
    private String actionDetail;

    /**
     * 执行状态: SUCCESS, FAILED, SKIPPED
     */
    private String status;

    /**
     * 执行结果消息
     */
    private String resultMessage;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时(毫秒)
     */
    private Long duration;

    /**
     * 执行开始时间
     */
    private LocalDateTime executedAt;

    /**
     * 请求数据 (JSON格式)
     */
    private String requestData;

    /**
     * 响应数据 (JSON格式)
     */
    private String responseData;
}