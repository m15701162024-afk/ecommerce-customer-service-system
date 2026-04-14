package com.ecommerce.workorder.controller;

import com.ecommerce.common.result.PageResult;
import com.ecommerce.common.result.Result;
import com.ecommerce.workorder.dto.request.*;
import com.ecommerce.workorder.dto.response.*;
import com.ecommerce.workorder.entity.WorkOrder;
import com.ecommerce.workorder.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单控制器
 */
@Tag(name = "工单管理", description = "工单相关接口")
@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
@Validated
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    /**
     * 创建工单
     */
    @Operation(summary = "创建工单", description = "创建新的工单")
    @PostMapping
    public Result<Long> createWorkOrder(
            @Valid @RequestBody CreateWorkOrderRequest request,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @Parameter(description = "操作人名称") @RequestHeader(value = "X-User-Name", required = false) String operatorName) {
        
        Long id = workOrderService.createWorkOrder(request, operatorId, operatorName);
        return Result.success(id);
    }

    /**
     * 工单列表(分页)
     */
    @Operation(summary = "工单列表", description = "分页查询工单列表")
    @GetMapping("/list")
    public Result<PageResult<WorkOrderResponse>> listWorkOrders(QueryWorkOrderRequest request) {
        PageResult<WorkOrderResponse> result = workOrderService.listWorkOrders(request);
        return Result.success(result);
    }

    /**
     * 工单详情
     */
    @Operation(summary = "工单详情", description = "获取工单详情")
    @GetMapping("/{id}")
    public Result<WorkOrderDetailResponse> getWorkOrderDetail(
            @Parameter(description = "工单ID") @PathVariable @NotNull Long id) {
        
        WorkOrderDetailResponse detail = workOrderService.getWorkOrderDetail(id);
        return Result.success(detail);
    }

    /**
     * 分配工单
     */
    @Operation(summary = "分配工单", description = "将工单分配给指定客服")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'CUSTOMER_SERVICE')")
    @PostMapping("/assign")
    public Result<Void> assignWorkOrder(
            @Valid @RequestBody AssignWorkOrderRequest request,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @Parameter(description = "操作人名称") @RequestHeader(value = "X-User-Name", required = false) String operatorName) {
        
        workOrderService.assignWorkOrder(request, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 转派工单
     */
    @Operation(summary = "转派工单", description = "将工单转派给其他客服")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'CUSTOMER_SERVICE')")
    @PostMapping("/transfer")
    public Result<Void> transferWorkOrder(
            @Valid @RequestBody TransferWorkOrderRequest request,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @Parameter(description = "操作人名称") @RequestHeader(value = "X-User-Name", required = false) String operatorName) {
        
        workOrderService.transferWorkOrder(request, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 开始处理工单
     */
    @Operation(summary = "开始处理", description = "标记工单为处理中状态")
    @PostMapping("/{id}/start-processing")
    public Result<Void> startProcessing(
            @Parameter(description = "工单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @Parameter(description = "操作人名称") @RequestHeader(value = "X-User-Name", required = false) String operatorName) {
        
        workOrderService.startProcessing(id, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 解决工单
     */
    @Operation(summary = "解决工单", description = "标记工单为已解决状态")
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN', 'TEAM_LEAD')")
    @PostMapping("/resolve")
    public Result<Void> resolveWorkOrder(
            @Valid @RequestBody ResolveWorkOrderRequest request,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @Parameter(description = "操作人名称") @RequestHeader(value = "X-User-Name", required = false) String operatorName) {
        
        workOrderService.resolveWorkOrder(request, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 关闭工单
     */
    @Operation(summary = "关闭工单", description = "关闭工单并填写满意度评价")
    @PreAuthorize("hasAnyRole('CUSTOMER_SERVICE', 'ADMIN', 'TEAM_LEAD')")
    @PostMapping("/close")
    public Result<Void> closeWorkOrder(
            @Valid @RequestBody CloseWorkOrderRequest request,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @Parameter(description = "操作人名称") @RequestHeader(value = "X-User-Name", required = false) String operatorName) {
        
        workOrderService.closeWorkOrder(request, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 重开工单
     */
    @Operation(summary = "重开工单", description = "重新打开已关闭的工单")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD', 'CUSTOMER_SERVICE')")
    @PostMapping("/{id}/reopen")
    public Result<Void> reopenWorkOrder(
            @Parameter(description = "工单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "重开原因") @RequestParam(required = false) String reason,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @Parameter(description = "操作人名称") @RequestHeader(value = "X-User-Name", required = false) String operatorName) {
        
        workOrderService.reopenWorkOrder(id, reason, operatorId, operatorName);
        return Result.success();
    }

    /**
     * 工单统计
     */
    @Operation(summary = "工单统计", description = "获取工单统计数据")
    @GetMapping("/stats")
    public Result<WorkOrderStatsResponse> getStats(
            @Parameter(description = "店铺ID") @RequestParam(required = false) Long shopId,
            @Parameter(description = "团队ID") @RequestParam(required = false) Long teamId,
            @Parameter(description = "处理人ID") @RequestParam(required = false) Long assigneeId) {
        
        WorkOrderStatsResponse stats = workOrderService.getStats(shopId, teamId, assigneeId);
        return Result.success(stats);
    }

    /**
     * 超时工单列表
     */
    @Operation(summary = "超时工单", description = "获取已超时的工单列表")
    @GetMapping("/overdue")
    public Result<List<WorkOrder>> getOverdueOrders() {
        List<WorkOrder> orders = workOrderService.getOverdueOrders();
        return Result.success(orders);
    }

    /**
     * 即将超时工单列表
     */
    @Operation(summary = "即将超时工单", description = "获取即将超时的工单列表")
    @GetMapping("/near-due")
    public Result<List<WorkOrder>> getNearDueOrders(
            @Parameter(description = "提前小时数") @RequestParam(defaultValue = "1") Integer hoursBefore) {
        
        List<WorkOrder> orders = workOrderService.getNearDueOrders(hoursBefore);
        return Result.success(orders);
    }
}