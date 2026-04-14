package com.ecommerce.purchase.controller;

import com.ecommerce.common.idempotent.Idempotent;
import com.ecommerce.purchase.entity.PurchaseOrder;
import com.ecommerce.purchase.entity.PurchaseOrder.OrderStatus;
import com.ecommerce.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 采购接口控制器
 */
@Tag(name = "采购服务", description = "采购订单管理接口")
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * 获取采购订单列表
     */
    @Operation(summary = "获取采购订单列表", description = "返回所有采购订单，按创建时间倒序排列")
    @GetMapping("/list")
    public ResponseEntity<List<PurchaseOrder>> getPurchaseList(
            @Parameter(description = "订单状态筛选") @RequestParam(required = false) OrderStatus status) {
        List<PurchaseOrder> orders;
        if (status != null) {
            orders = purchaseService.getOrdersByStatus(status);
        } else {
            orders = purchaseService.getAllOrders();
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * 创建采购订单
     */
    @Operation(summary = "创建采购订单", description = "创建新的采购订单，根据金额自动判断处理方式")
    @PreAuthorize("hasAnyRole('PURCHASER', 'ADMIN')")
    @Idempotent(key = "#order.productName + ':' + #order.productId", expireTime = 300, message = "该商品采购订单已提交，请勿重复提交")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPurchase(
            @RequestBody PurchaseOrder order) {
        PurchaseOrder createdOrder = purchaseService.createOrder(order);
        
        String message = getOrderStatusMessage(createdOrder);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", message,
            "data", createdOrder
        ));
    }

    /**
     * 获取采购统计
     */
    @Operation(summary = "获取采购统计", description = "返回采购订单的统计数据")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPurchaseStats() {
        Map<String, Object> stats = purchaseService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 确认采购订单
     */
    @Operation(summary = "确认采购订单", description = "确认待确认状态的采购订单")
    @PreAuthorize("hasAnyRole('PURCHASER', 'ADMIN', 'FINANCE')")
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        PurchaseOrder order = purchaseService.confirmOrder(orderId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "订单确认成功",
            "data", order
        ));
    }

    /**
     * 取消采购订单
     */
    @Operation(summary = "取消采购订单", description = "取消未完成的采购订单")
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        PurchaseOrder order = purchaseService.cancelOrder(orderId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "订单已取消",
            "data", order
        ));
    }

    /**
     * 拒绝采购订单
     */
    @Operation(summary = "拒绝采购订单", description = "拒绝待确认的采购订单")
    @PreAuthorize("hasAnyRole('PURCHASER', 'ADMIN', 'FINANCE')")
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<Map<String, Object>> rejectOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        PurchaseOrder order = purchaseService.rejectOrder(orderId, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "订单已拒绝",
            "data", order
        ));
    }

    /**
     * 获取待人工确认的订单列表
     */
    @Operation(summary = "待人工确认列表", description = "获取需要人工确认的采购订单")
    @GetMapping("/manual-confirm-list")
    public ResponseEntity<List<PurchaseOrder>> getManualConfirmList() {
        List<PurchaseOrder> orders = purchaseService.getManualConfirmOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据订单状态返回提示消息
     */
    private String getOrderStatusMessage(PurchaseOrder order) {
        return switch (order.getStatus()) {
            case AUTO_PAID -> "采购订单创建成功，金额小于1000元，已自动支付";
            case CONFIRMING -> "采购订单创建成功，金额在1000-10000元之间，等待人工确认";
            case WARNING -> "警告：采购订单金额超过10000元，请及时确认处理！";
            default -> "采购订单创建成功";
        };
    }
}