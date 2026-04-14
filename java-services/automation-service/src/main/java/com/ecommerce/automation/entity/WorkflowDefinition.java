package com.ecommerce.automation.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 工作流定义实体
 * 定义可复用的工作流模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_workflow_definition", autoResultMap = true)
public class WorkflowDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流编码 (唯一标识)
     */
    private String code;

    /**
     * 店铺ID (null表示全局工作流)
     */
    private Long shopId;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 触发条件
     * {"triggerType": "EVENT|MANUAL|SCHEDULE", "conditions": {...}}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> triggerCondition;

    /**
     * 工作流步骤定义 (JSON格式)
     * [
     *   {"stepId": "step1", "type": "ACTION", "actionType": "SEND_MESSAGE", "config": {...}},
     *   {"stepId": "step2", "type": "CONDITION", "expression": "...", "trueStep": "step3", "falseStep": "step4"},
     *   {"stepId": "step3", "type": "ACTION", "actionType": "WAIT", "config": {"duration": 300}},
     *   {"stepId": "step4", "type": "ACTION", "actionType": "TRANSFER", "config": {"agentGroup": "售后组"}}
     * ]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> steps;

    /**
     * 变量定义
     * {"variables": [{"name": "orderNo", "source": "event.data.orderNo", "type": "STRING"}]}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> variables;

    /**
     * 起始步骤ID
     */
    private String startStepId;

    /**
     * 状态: DRAFT, PUBLISHED, ARCHIVED
     */
    private String status;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;
}