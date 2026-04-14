package com.ecommerce.purchase.service;

import com.ecommerce.purchase.client.InventoryServiceClient;
import com.ecommerce.purchase.entity.PurchaseOrder;
import com.ecommerce.purchase.entity.PurchaseOrder.OrderStatus;
import com.ecommerce.purchase.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 采购服务业务逻辑
 * 
 * 业务规则：
 * - 小于1000元: 自动支付
 * - 1000-10000元: 需人工确认
 * - 大于10000元: 需人工确认并预警
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    // 金额阈值常量
    private static final BigDecimal AUTO_PAY_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("10000");

    /**
     * 创建采购订单
     */
    @Transactional
    public PurchaseOrder createOrder(PurchaseOrder order) {
        // 生成订单号
        order.setOrderNo(generateOrderNo());
        
        // 计算总金额
        BigDecimal totalAmount = order.getUnitPrice().multiply(new BigDecimal(order.getQuantity()));
        order.setTotalAmount(totalAmount);
        
        // 设置创建时间
        order.setCreateTime(LocalDateTime.now());
        
        // 根据金额判断状态
        OrderStatus status = determineOrderStatus(totalAmount);
        order.setStatus(status);
        
        // 如果是自动支付，设置支付时间
        if (status == OrderStatus.AUTO_PAID) {
            order.setPayTime(LocalDateTime.now());
            log.info("订单 {} 金额 {} 元，自动支付成功", order.getOrderNo(), totalAmount);
            
            // PU-002: 自动支付订单扣减库存
            deductInventory(order);
        } else if (status == OrderStatus.CONFIRMING) {
            log.info("订单 {} 金额 {} 元，需人工确认", order.getOrderNo(), totalAmount);
        } else if (status == OrderStatus.WARNING) {
            log.warn("订单 {} 金额 {} 元，超过预警阈值，需人工确认!", order.getOrderNo(), totalAmount);
        }
        
        order.setUpdateTime(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    /**
     * 根据金额判断订单状态
     */
    private OrderStatus determineOrderStatus(BigDecimal amount) {
        if (amount.compareTo(AUTO_PAY_THRESHOLD) < 0) {
            return OrderStatus.AUTO_PAID;
        } else if (amount.compareTo(WARNING_THRESHOLD) <= 0) {
            return OrderStatus.CONFIRMING;
        } else {
            return OrderStatus.WARNING;
        }
    }

    /**
     * 获取所有采购订单
     */
    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAllByOrderByCreateTimeDesc();
    }

    /**
     * 根据状态获取订单
     */
    public List<PurchaseOrder> getOrdersByStatus(OrderStatus status) {
        return purchaseOrderRepository.findByStatus(status);
    }

    /**
     * 确认订单
     */
    @Transactional
    public PurchaseOrder confirmOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
        
        if (order.getStatus() != OrderStatus.CONFIRMING && order.getStatus() != OrderStatus.WARNING) {
            throw new RuntimeException("订单状态不允许确认");
        }
        
        order.setStatus(OrderStatus.COMPLETED);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // PU-002: 确认订单后扣减库存
        deductInventory(order);
        
        log.info("订单 {} 已确认支付", order.getOrderNo());
        return purchaseOrderRepository.save(order);
    }

    /**
     * 取消订单
     */
    @Transactional
    public PurchaseOrder cancelOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
        
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("已完成的订单不能取消");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdateTime(LocalDateTime.now());
        
        log.info("订单 {} 已取消", order.getOrderNo());
        return purchaseOrderRepository.save(order);
    }

    /**
     * 拒绝采购订单
     */
    @Transactional
    public PurchaseOrder rejectOrder(Long orderId, String reason) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
        
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("订单状态不允许拒绝");
        }
        
        order.setStatus(OrderStatus.REJECTED);
        order.setRemark(reason);
        order.setUpdateTime(LocalDateTime.now());
        
        log.info("订单 {} 已拒绝, 原因: {}", order.getOrderNo(), reason);
        return purchaseOrderRepository.save(order);
    }

    /**
     * 获取待人工确认的订单列表
     */
    public List<PurchaseOrder> getManualConfirmOrders() {
        return purchaseOrderRepository.findByStatusIn(
            List.of(OrderStatus.CONFIRMING, OrderStatus.WARNING)
        );
    }

    /**
     * 获取采购统计信息
     */
    public Map<String, Object> getStatistics() {
        List<PurchaseOrder> allOrders = purchaseOrderRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总订单数
        stats.put("totalOrders", allOrders.size());
        
        // 各状态订单数
        Map<String, Long> statusCount = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusCount.put(status.name(), 
                allOrders.stream().filter(o -> o.getStatus() == status).count());
        }
        stats.put("statusCount", statusCount);
        
        // 总金额
        BigDecimal totalAmount = allOrders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalAmount", totalAmount);
        
        // 已支付金额 (自动支付 + 已完成)
        BigDecimal paidAmount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.AUTO_PAID || 
                             o.getStatus() == OrderStatus.COMPLETED)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("paidAmount", paidAmount);
        
        // 待确认金额
        BigDecimal pendingAmount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMING || 
                             o.getStatus() == OrderStatus.WARNING)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("pendingAmount", pendingAmount);
        
        // 预警订单数
        long warningCount = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.WARNING)
                .count();
        stats.put("warningCount", warningCount);
        
        return stats;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PO" + timestamp + random;
    }

    /**
     * PU-002: 扣减库存
     * 当采购订单支付完成后扣减库存
     */
    private void deductInventory(PurchaseOrder order) {
        if (order.getProductId() == null) {
            log.warn("订单 {} 缺少商品ID，跳过库存扣减", order.getOrderNo());
            return;
        }
        
        try {
            Boolean success = inventoryServiceClient.deductStock(
                order.getProductId(), 
                order.getQuantity(), 
                order.getOrderNo()
            );
            if (Boolean.TRUE.equals(success)) {
                log.info("库存扣减成功, productId={}, quantity={}, orderNo={}", 
                    order.getProductId(), order.getQuantity(), order.getOrderNo());
            } else {
                log.warn("库存扣减返回失败, productId={}, orderNo={}", 
                    order.getProductId(), order.getOrderNo());
            }
        } catch (Exception e) {
            log.error("库存扣减失败, productId={}, orderNo={}, error={}", 
                order.getProductId(), order.getOrderNo(), e.getMessage());
        }
    }
}