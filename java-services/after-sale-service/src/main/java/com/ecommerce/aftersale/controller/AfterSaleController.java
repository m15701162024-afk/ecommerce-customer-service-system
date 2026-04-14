package com.ecommerce.aftersale.controller;

import com.ecommerce.aftersale.dto.request.*;
import com.ecommerce.aftersale.dto.response.*;
import com.ecommerce.aftersale.entity.ReturnAddress;
import com.ecommerce.aftersale.service.AfterSaleService;
import com.ecommerce.common.idempotent.Idempotent;
import com.ecommerce.common.result.PageResult;
import com.ecommerce.common.result.Result;
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
 * 售后控制器
 */
@Tag(name = "售后管理", description = "售后相关接口")
@RestController
@RequestMapping("/after-sales")
@RequiredArgsConstructor
@Validated
public class AfterSaleController {

    private final AfterSaleService afterSaleService;

    /**
     * 申请售后
     */
    @Operation(summary = "申请售后", description = "买家申请售后(仅退款/退货退款/换货)")
    @Idempotent(key = "#request.orderId + ':' + #request.type", expireTime = 300, message = "该订单已提交售后申请，请勿重复提交")
    @PostMapping("/apply")
    public Result<Long> applyAfterSale(
            @Parameter(description = "申请请求") @Valid @RequestBody ApplyAfterSaleRequest request,
            @Parameter(description = "用户ID") @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Parameter(description = "店铺ID") @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            @Parameter(description = "平台") @RequestHeader(value = "X-Platform", required = false) String platform) {

        // 默认值处理
        if (userId == null) userId = 1L;
        if (shopId == null) shopId = 1L;
        if (platform == null) platform = "default";

        Long id = afterSaleService.applyAfterSale(request, userId, shopId, platform);
        return Result.success(id);
    }

    /**
     * 售后列表
     */
    @Operation(summary = "售后列表", description = "分页查询售后列表")
    @GetMapping("/list")
    public Result<PageResult<AfterSaleListResponse>> listAfterSales(
            @Parameter(description = "查询参数") AfterSaleQueryRequest request) {

        PageResult<AfterSaleListResponse> result = afterSaleService.listAfterSales(request);
        return Result.success(result);
    }

    /**
     * 售后详情
     */
    @Operation(summary = "售后详情", description = "根据ID获取售后详情")
    @GetMapping("/{id}")
    public Result<AfterSaleDetailResponse> getAfterSaleDetail(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id) {

        AfterSaleDetailResponse detail = afterSaleService.getAfterSaleDetail(id);
        return Result.success(detail);
    }

    /**
     * 审批售后
     */
    @Operation(summary = "审批售后", description = "卖家审批售后申请(同意/拒绝)")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'CUSTOMER_SERVICE')")
    @PostMapping("/{id}/approve")
    public Result<Void> approveAfterSale(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "审批请求") @Valid @RequestBody ApproveAfterSaleRequest request,
            @Parameter(description = "审批人ID") @RequestHeader(value = "X-User-Id", required = false) Long approverId,
            @Parameter(description = "审批人姓名") @RequestHeader(value = "X-User-Name", required = false) String approverName) {

        // 默认值处理
        if (approverId == null) approverId = 1L;
        if (approverName == null) approverName = "客服";

        afterSaleService.approveAfterSale(id, request, approverId, approverName);
        return Result.success();
    }

    /**
     * 填写退货物流
     */
    @Operation(summary = "填写退货物流", description = "买家填写退货物流信息")
    @PostMapping("/{id}/return-logistics")
    public Result<Void> fillReturnLogistics(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "物流信息") @Valid @RequestBody FillLogisticsRequest request) {

        afterSaleService.fillReturnLogistics(id, request);
        return Result.success();
    }

    /**
     * 确认收货
     */
    @Operation(summary = "确认收货", description = "卖家确认收到退货")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'CUSTOMER_SERVICE')")
    @PostMapping("/{id}/confirm-received")
    public Result<Void> confirmReturnReceived(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id) {

        afterSaleService.confirmReturnReceived(id);
        return Result.success();
    }

    /**
     * 执行退款
     */
    @Operation(summary = "执行退款", description = "执行退款操作")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @PostMapping("/{id}/refund")
    public Result<Void> executeRefund(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id) {

        afterSaleService.executeRefund(id);
        return Result.success();
    }

    /**
     * 换货发货
     */
    @Operation(summary = "换货发货", description = "卖家发换货商品")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'WAREHOUSE')")
    @PostMapping("/{id}/exchange-ship")
    public Result<Void> shipExchangeGoods(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "发货信息") @Valid @RequestBody ExchangeShipRequest request) {

        afterSaleService.shipExchangeGoods(id, request);
        return Result.success();
    }

    /**
     * 取消售后
     */
    @Operation(summary = "取消售后", description = "买家取消售后申请")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelAfterSale(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id) {

        afterSaleService.cancelAfterSale(id);
        return Result.success();
    }

    /**
     * 添加卖家备注
     */
    @Operation(summary = "添加卖家备注", description = "添加卖家备注")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'CUSTOMER_SERVICE')")
    @PostMapping("/{id}/seller-remark")
    public Result<Void> addSellerRemark(
            @Parameter(description = "售后单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "备注内容") @RequestParam String remark) {

        afterSaleService.addSellerRemark(id, remark);
        return Result.success();
    }

    /**
     * 售后原因列表
     */
    @Operation(summary = "售后原因列表", description = "获取售后原因列表")
    @GetMapping("/reasons")
    public Result<List<AfterSaleReasonResponse>> listReasons(
            @Parameter(description = "类型: refund-退款, return-退货, exchange-换货") 
            @RequestParam(required = false) String type) {

        List<AfterSaleReasonResponse> list = afterSaleService.listReasons(type);
        return Result.success(list);
    }

    /**
     * 售后统计
     */
    @Operation(summary = "售后统计", description = "获取售后统计数据")
    @GetMapping("/statistics")
    public Result<AfterSaleStatisticsResponse> getStatistics() {

        AfterSaleStatisticsResponse statistics = afterSaleService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 退货地址列表
     */
    @Operation(summary = "退货地址列表", description = "获取店铺退货地址列表")
    @GetMapping("/return-addresses")
    public Result<List<ReturnAddress>> listReturnAddresses(
            @Parameter(description = "店铺ID") @RequestParam(required = false) Long shopId) {

        // 默认值处理
        if (shopId == null) shopId = 1L;

        List<ReturnAddress> list = afterSaleService.listReturnAddresses(shopId);
        return Result.success(list);
    }
}