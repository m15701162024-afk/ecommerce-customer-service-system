package com.ecommerce.order.controller;

import com.ecommerce.common.idempotent.Idempotent;
import com.ecommerce.common.result.PageResult;
import com.ecommerce.common.result.Result;
import com.ecommerce.order.dto.OrderCreateRequest;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
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
import java.util.Map;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    /**
     * 订单列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   订单状态
     * @param userId   用户ID
     * @param shopId   店铺ID
     * @return 分页订单列表
     */
    @Operation(summary = "订单列表", description = "分页查询订单列表")
    @GetMapping("/list")
    public Result<PageResult<Order>> listOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "订单状态") @RequestParam(required = false) String status,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "店铺ID") @RequestParam(required = false) Long shopId) {
        
        PageResult<Order> result = orderService.listOrders(pageNum, pageSize, status, userId, shopId);
        return Result.success(result);
    }

    /**
     * 订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    @Operation(summary = "订单详情", description = "根据ID获取订单详情")
    @GetMapping("/{id}")
    public Result<Order> getOrderById(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long id) {
        
        Order order = orderService.getOrderById(id);
        return Result.success(order);
    }

    /**
     * 更新订单状态
     *
     * @param id     订单ID
     * @param status 新状态
     * @param remark 备注
     * @return 操作结果
     */
    @Operation(summary = "更新订单状态", description = "更新订单状态")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('order:manage')")
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "新状态") @RequestParam @NotNull String status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        orderService.updateOrderStatus(id, status, remark);
        return Result.success();
    }

    /**
     * 根据订单编号查询
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    @Operation(summary = "按订单编号查询", description = "根据订单编号获取订单详情")
    @GetMapping("/no/{orderNo}")
    public Result<Order> getOrderByOrderNo(
            @Parameter(description = "订单编号") @PathVariable String orderNo) {
        
        Order order = orderService.getOrderByOrderNo(orderNo);
        return Result.success(order);
    }

    /**
     * 创建订单（带库存验证）
     *
     * @param request 创建订单请求
     * @return 订单ID
     */
    @Operation(summary = "创建订单", description = "创建订单并验证库存，预留库存")
    @Idempotent(key = "#request.userId + ':' + #request.shopId + ':' + (#request.externalOrderNo ?: 'new')", expireTime = 300)
    @PostMapping
    public Result<Long> createOrder(
            @Parameter(description = "创建订单请求") @RequestBody @Valid OrderCreateRequest request) {
        
        Long orderId = orderService.createOrderWithInventoryCheck(request);
        return Result.success(orderId);
    }

    /**
     * 更新订单状态（带库存操作）
 *
     * @param id     订单ID
     * @param status 新状态
     * @param remark 备注
     * @param items  订单商品项（支付/取消时用于库存操作）
     * @return 操作结果
     */
    @Operation(summary = "更新订单状态(含库存)", description = "更新订单状态并处理库存扣减/释放")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('order:manage')")
    @PutMapping("/{id}/status-with-inventory")
    public Result<Void> updateOrderStatusWithInventory(
            @Parameter(description = "订单ID") @PathVariable @NotNull Long id,
            @Parameter(description = "新状态") @RequestParam @NotNull String status,
            @Parameter(description = "备注") @RequestParam(required = false) String remark,
            @Parameter(description = "订单商品项") @RequestBody(required = false) List<OrderItemDTO> items) {
        
        orderService.updateOrderStatusWithInventory(id, status, remark, items);
        return Result.success();
    }

    /**
     * 获取订单趋势数据
     *
     * @param range 时间范围 (week/month)
     * @return 趋势数据
     */
    @Operation(summary = "订单趋势", description = "获取订单趋势数据，支持近7天和近30天")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getOrderTrend(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String range) {
        
        Map<String, Object> trendData = orderService.getOrderTrend(range);
        return Result.success(trendData);
    }

    /**
     * 获取平台订单分布
     *
     * @return 平台分布数据
     */
    @Operation(summary = "平台订单分布", description = "获取各平台订单数量分布")
    @GetMapping("/platform-distribution")
    public Result<List<Map<String, Object>>> getPlatformDistribution() {
        
        List<Map<String, Object>> distribution = orderService.getPlatformDistribution();
        return Result.success(distribution);
    }
}