package com.ecommerce.automation.controller;

import com.ecommerce.automation.dto.RuleCreateRequest;
import com.ecommerce.automation.dto.RuleUpdateRequest;
import com.ecommerce.automation.dto.WorkflowCreateRequest;
import com.ecommerce.automation.entity.AutoRule;
import com.ecommerce.automation.entity.ExecutionLog;
import com.ecommerce.automation.entity.WorkflowDefinition;
import com.ecommerce.automation.entity.WorkflowInstance;
import com.ecommerce.automation.mapper.ExecutionLogMapper;
import com.ecommerce.automation.service.AutoRuleService;
import com.ecommerce.automation.service.TriggerService;
import com.ecommerce.automation.service.WorkflowService;
import com.ecommerce.common.result.PageResult;
import com.ecommerce.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自动化服务控制器
 */
@Tag(name = "自动化服务", description = "自动回复规则、工作流管理、触发器配置")
@RestController
@RequestMapping("/api/automation")
@RequiredArgsConstructor
public class AutomationController {

    private final AutoRuleService autoRuleService;
    private final WorkflowService workflowService;
    private final TriggerService triggerService;
    private final ExecutionLogMapper executionLogMapper;

    // ==================== 规则管理 ====================

    @Operation(summary = "创建自动回复规则")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PostMapping("/rules")
    public Result<Long> createRule(@Valid @RequestBody RuleCreateRequest request) {
        AutoRule rule = new AutoRule();
        rule.setName(request.getName());
        rule.setRuleType(request.getRuleType());
        rule.setShopId(request.getShopId());
        rule.setTriggerConfig(request.getTriggerConfig());
        rule.setActionConfig(request.getActionConfig());
        rule.setPriority(request.getPriority());
        rule.setDescription(request.getDescription());
        rule.setEffectiveFrom(request.getEffectiveFrom());
        rule.setEffectiveTo(request.getEffectiveTo());
        
        Long ruleId = autoRuleService.createRule(rule);
        return Result.success(ruleId);
    }

    @Operation(summary = "更新自动回复规则")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/rules/{id}")
    public Result<Void> updateRule(
            @Parameter(description = "规则ID") @PathVariable Long id,
            @Valid @RequestBody RuleUpdateRequest request) {
        AutoRule rule = new AutoRule();
        rule.setName(request.getName());
        rule.setRuleType(request.getRuleType());
        rule.setShopId(request.getShopId());
        rule.setTriggerConfig(request.getTriggerConfig());
        rule.setActionConfig(request.getActionConfig());
        rule.setPriority(request.getPriority());
        rule.setDescription(request.getDescription());
        rule.setEffectiveFrom(request.getEffectiveFrom());
        rule.setEffectiveTo(request.getEffectiveTo());
        
        autoRuleService.updateRule(id, rule);
        return Result.success();
    }

    @Operation(summary = "删除自动回复规则")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @DeleteMapping("/rules/{id}")
    public Result<Void> deleteRule(@Parameter(description = "规则ID") @PathVariable Long id) {
        autoRuleService.deleteRule(id);
        return Result.success();
    }

    @Operation(summary = "启用规则")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/rules/{id}/enable")
    public Result<Void> enableRule(@Parameter(description = "规则ID") @PathVariable Long id) {
        autoRuleService.enableRule(id);
        return Result.success();
    }

    @Operation(summary = "禁用规则")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/rules/{id}/disable")
    public Result<Void> disableRule(@Parameter(description = "规则ID") @PathVariable Long id) {
        autoRuleService.disableRule(id);
        return Result.success();
    }

    @Operation(summary = "获取规则详情")
    @GetMapping("/rules/{id}")
    public Result<AutoRule> getRule(@Parameter(description = "规则ID") @PathVariable Long id) {
        return Result.success(autoRuleService.getRuleById(id));
    }

    @Operation(summary = "分页查询规则列表")
    @GetMapping("/rules")
    public Result<PageResult<AutoRule>> listRules(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "规则类型") @RequestParam(required = false) String ruleType,
            @Parameter(description = "店铺ID") @RequestParam(required = false) Long shopId,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        return Result.success(autoRuleService.listRules(pageNum, pageSize, ruleType, shopId, status));
    }

    // ==================== 工作流管理 ====================

    @Operation(summary = "创建工作流定义")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PostMapping("/workflows")
    public Result<Long> createWorkflow(@Valid @RequestBody WorkflowCreateRequest request) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName(request.getName());
        definition.setCode(request.getCode());
        definition.setShopId(request.getShopId());
        definition.setDescription(request.getDescription());
        definition.setTriggerCondition(request.getTriggerCondition());
        definition.setSteps(request.getSteps());
        definition.setVariables(request.getVariables());
        definition.setStartStepId(request.getStartStepId());
        definition.setCreatedBy(request.getCreatedBy());
        
        Long workflowId = workflowService.createWorkflowDefinition(definition);
        return Result.success(workflowId);
    }

    @Operation(summary = "更新工作流定义")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/workflows/{id}")
    public Result<Void> updateWorkflow(
            @Parameter(description = "工作流ID") @PathVariable Long id,
            @Valid @RequestBody WorkflowCreateRequest request) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName(request.getName());
        definition.setCode(request.getCode());
        definition.setShopId(request.getShopId());
        definition.setDescription(request.getDescription());
        definition.setTriggerCondition(request.getTriggerCondition());
        definition.setSteps(request.getSteps());
        definition.setVariables(request.getVariables());
        definition.setStartStepId(request.getStartStepId());
        
        workflowService.updateWorkflowDefinition(id, definition);
        return Result.success();
    }

    @Operation(summary = "发布工作流")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/workflows/{id}/publish")
    public Result<Void> publishWorkflow(@Parameter(description = "工作流ID") @PathVariable Long id) {
        workflowService.publishWorkflowDefinition(id);
        return Result.success();
    }

    @Operation(summary = "归档工作流")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/workflows/{id}/archive")
    public Result<Void> archiveWorkflow(@Parameter(description = "工作流ID") @PathVariable Long id) {
        workflowService.archiveWorkflowDefinition(id);
        return Result.success();
    }

    @Operation(summary = "获取工作流定义详情")
    @GetMapping("/workflows/{id}")
    public Result<WorkflowDefinition> getWorkflow(@Parameter(description = "工作流ID") @PathVariable Long id) {
        return Result.success(workflowService.getWorkflowDefinitionById(id));
    }

    @Operation(summary = "分页查询工作流定义")
    @GetMapping("/workflows")
    public Result<PageResult<WorkflowDefinition>> listWorkflows(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "店铺ID") @RequestParam(required = false) Long shopId,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        return Result.success(workflowService.listWorkflowDefinitions(pageNum, pageSize, shopId, status));
    }

    // ==================== 工作流实例 ====================

    @Operation(summary = "启动工作流实例")
    @PostMapping("/workflows/{definitionId}/start")
    public Result<Long> startWorkflow(
            @Parameter(description = "工作流定义ID") @PathVariable Long definitionId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String businessId,
            @RequestParam(required = false) String businessType,
            @RequestParam(defaultValue = "MANUAL") String triggerSource) {
        Long instanceId = workflowService.startWorkflowInstance(
            definitionId, sessionId, shopId, userId, businessId, businessType, triggerSource);
        return Result.success(instanceId);
    }

    @Operation(summary = "暂停工作流实例")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/instances/{instanceId}/pause")
    public Result<Void> pauseWorkflowInstance(@PathVariable Long instanceId) {
        workflowService.pauseWorkflowInstance(instanceId);
        return Result.success();
    }

    @Operation(summary = "恢复工作流实例")
    @PutMapping("/instances/{instanceId}/resume")
    public Result<Void> resumeWorkflowInstance(@PathVariable Long instanceId) {
        workflowService.resumeWorkflowInstance(instanceId);
        return Result.success();
    }

    @Operation(summary = "取消工作流实例")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTOMATION_MANAGER')")
    @PutMapping("/instances/{instanceId}/cancel")
    public Result<Void> cancelWorkflowInstance(@PathVariable Long instanceId) {
        workflowService.cancelWorkflowInstance(instanceId);
        return Result.success();
    }

    @Operation(summary = "获取工作流实例详情")
    @GetMapping("/instances/{instanceId}")
    public Result<WorkflowInstance> getWorkflowInstance(@PathVariable Long instanceId) {
        return Result.success(workflowService.getWorkflowInstanceById(instanceId));
    }

    @Operation(summary = "查询工作流实例列表")
    @GetMapping("/instances")
    public Result<PageResult<WorkflowInstance>> listWorkflowInstances(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long definitionId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String status) {
        return Result.success(workflowService.listWorkflowInstances(pageNum, pageSize, definitionId, shopId, status));
    }

    // ==================== 执行日志 ====================

    @Operation(summary = "查询规则执行日志")
    @GetMapping("/rules/{ruleId}/logs")
    public Result<List<ExecutionLog>> getRuleExecutionLogs(
            @PathVariable Long ruleId,
            @RequestParam(defaultValue = "100") int limit) {
        return Result.success(executionLogMapper.findByRuleId(ruleId, limit));
    }

    @Operation(summary = "查询工作流执行日志")
    @GetMapping("/instances/{instanceId}/logs")
    public Result<List<ExecutionLog>> getWorkflowExecutionLogs(@PathVariable Long instanceId) {
        return Result.success(executionLogMapper.findByWorkflowInstanceId(instanceId));
    }

    // ==================== 触发器测试 ====================

    @Operation(summary = "测试关键词触发")
    @PostMapping("/test/keyword")
    public Result<Boolean> testKeywordTrigger(
            @RequestParam Long sessionId,
            @RequestParam Long shopId,
            @RequestParam Long userId,
            @RequestParam String message) {
        boolean triggered = triggerService.processKeywordTrigger(sessionId, shopId, userId, message);
        return Result.success(triggered);
    }

    @Operation(summary = "触发欢迎语")
    @PostMapping("/test/welcome")
    public Result<Void> triggerWelcome(
            @RequestParam Long sessionId,
            @RequestParam Long shopId,
            @RequestParam Long userId) {
        triggerService.handleFirstConsultation(sessionId, shopId, userId);
        return Result.success();
    }

    @Operation(summary = "发送订单状态通知")
    @PostMapping("/test/order-notification")
    public Result<Void> sendOrderNotification(
            @RequestParam Long shopId,
            @RequestParam Long userId,
            @RequestParam String orderNo,
            @RequestParam String oldStatus,
            @RequestParam String newStatus) {
        triggerService.sendOrderStatusNotification(shopId, userId, orderNo, oldStatus, newStatus);
        return Result.success();
    }
}