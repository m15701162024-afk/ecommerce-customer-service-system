package com.ecommerce.automation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 工作流创建请求
 */
@Data
public class WorkflowCreateRequest {

    /**
     * 工作流名称
     */
    @NotBlank(message = "工作流名称不能为空")
    private String name;

    /**
     * 工作流编码
     */
    @NotBlank(message = "工作流编码不能为空")
    private String code;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 描述
     */
    private String description;

    /**
     * 触发条件
     */
    private Map<String, Object> triggerCondition;

    /**
     * 工作流步骤
     */
    private List<Map<String, Object>> steps;

    /**
     * 变量定义
     */
    private Map<String, Object> variables;

    /**
     * 起始步骤ID
     */
    private String startStepId;

    /**
     * 创建人ID
     */
    private Long createdBy;
}