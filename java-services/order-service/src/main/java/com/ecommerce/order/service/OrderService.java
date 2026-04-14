package com.ecommerce.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.audit.AuditLog;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.lock.DistributedLock;
import com.ecommerce.common.result.PageResult;
import com.ecommerce.order.client.InventoryServiceClient;
import com.ecommerce.order.dto.OrderCreateRequest;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final InventoryServiceClient inventoryServiceClient;
    private final DistributedLock distributedLock;

    /**
     * 分页查询订单列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   订单状态
     * @param userId   用户ID
     * @param shopId   店铺ID
     * @return 分页结果
     */
    public PageResult<Order> listOrders(Integer pageNum, Integer pageSize, String status, Long userId, Long shopId) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (shopId != null) {
            wrapper.eq(Order::getShopId, shopId);
        }
        
        wrapper.orderByDesc(Order::getCreatedAt);
        
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        
        return PageResult.of(
            result.getRecords(),
            result.getTotal(),
            result.getCurrent(),
            result.getSize()
        );
    }

    /**
     * 根据ID获取订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    public Order getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    /**
     * 根据订单编号获取订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    public Order getOrderByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    /**
     * 更新订单状态
     *
     * @param id     订单ID
     * @param status 新状态
     * @param remark 备注
     */
    @AuditLog(action = "状态变更", module = "订单", description = "订单状态变更")
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long id, String status, String remark) {
        Order order = getOrderById(id);
        
        // 验证状态流转
        validateStatusTransition(order.getStatus(), status);
        
        // 处理库存操作（根据状态流转）
        handleInventoryOperation(order, status);
        
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, id);
        updateWrapper.set(Order::getStatus, status);
        updateWrapper.set(Order::getUpdatedAt, LocalDateTime.now());
        
        // 根据状态设置相关时间字段
        if (OrderStatus.PAID.getCode().equals(status)) {
            updateWrapper.set(Order::getPaidAt, LocalDateTime.now());
        } else if (OrderStatus.SHIPPED.getCode().equals(status)) {
            updateWrapper.set(Order::getShippedAt, LocalDateTime.now());
        } else if (OrderStatus.COMPLETED.getCode().equals(status)) {
            updateWrapper.set(Order::getCompletedAt, LocalDateTime.now());
        }
        
        // 更新卖家备注
        if (StringUtils.hasText(remark)) {
            updateWrapper.set(Order::getSellerRemark, remark);
        }
        
        int rows = orderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("更新订单状态失败");
        }
        
        log.info("订单状态更新成功, orderId={}, oldStatus={}, newStatus={}", id, order.getStatus(), status);
    }

    /**
     * 处理库存操作
     * PAID: 扣减预留库存
     * CANCELLED: 释放预留库存
     */
    private void handleInventoryOperation(Order order, String newStatus) {
        // 从备注或其他方式获取订单商品信息（实际项目中应该从OrderItem表获取）
        // 这里简化处理，需要调用方传入商品信息或从数据库查询
        // 实际实现建议：创建OrderItem表存储商品明细
        
        if (OrderStatus.PAID.getCode().equals(newStatus)) {
            // 订单支付成功，扣减预留库存
            log.info("订单支付成功，开始扣减库存, orderId={}", order.getId());
            // TODO: 实际实现需要从OrderItem表获取商品列表并逐个扣减
            // 示例: inventoryServiceClient.deductStock(productId, sku, quantity);
        } else if (OrderStatus.CANCELLED.getCode().equals(newStatus)) {
            // 订单取消，释放预留库存
            log.info("订单取消，开始释放预留库存, orderId={}", order.getId());
            // TODO: 实际实现需要从OrderItem表获取商品列表并逐个释放
            // 示例: inventoryServiceClient.releaseStock(productId, sku, quantity, orderId);
        }
    }

    /**
     * 验证状态流转是否合法
     *
     * @param currentStatus 当前状态
     * @param newStatus     新状态
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        OrderStatus current = OrderStatus.fromCode(currentStatus);
        OrderStatus target = OrderStatus.fromCode(newStatus);
        
        // 定义合法的状态流转
        List<OrderStatus> allowedTransitions = switch (current) {
            case PENDING -> List.of(OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID -> List.of(OrderStatus.PREPARING, OrderStatus.REFUNDING);
            case PREPARING -> List.of(OrderStatus.SHIPPED, OrderStatus.REFUNDING);
            case SHIPPED -> List.of(OrderStatus.DELIVERED, OrderStatus.REFUNDING);
            case DELIVERED -> List.of(OrderStatus.COMPLETED, OrderStatus.REFUNDING);
            case COMPLETED -> List.of();
            case CANCELLED -> List.of();
            case REFUNDING -> List.of(OrderStatus.REFUNDED);
            case REFUNDED -> List.of();
        };
        
        if (!allowedTransitions.contains(target)) {
            throw new BusinessException(
                String.format("订单状态不允许从 %s 变更为 %s", current.getName(), target.getName())
            );
        }
    }

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Order order) {
        // 设置初始状态
        order.setStatus(OrderStatus.PENDING.getCode());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setDeleted(0);
        
        orderMapper.insert(order);
        
        log.info("订单创建成功, orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        return order.getId();
    }

    /**
     * 创建订单（带库存验证和预留）
     *
     * @param request 创建订单请求
     * @return 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderWithInventoryCheck(OrderCreateRequest request) {
        String lockKey = "order:create:user:" + request.getUserId();
        
        boolean locked = distributedLock.tryLock(lockKey, 3000, 10000);
        if (!locked) {
            throw new BusinessException("订单创建请求正在处理中，请勿重复提交");
        }
        
        try {
            return createOrderWithInventoryCheckInternal(request);
        } finally {
            distributedLock.unlock(lockKey);
        }
    }
    
    private Long createOrderWithInventoryCheckInternal(OrderCreateRequest request) {
        for (OrderItemDTO item : request.getItems()) {
            Boolean stockAvailable = inventoryServiceClient.checkStock(
                item.getProductId(),
                item.getSku(),
                item.getQuantity()
            );
            if (stockAvailable == null || !stockAvailable) {
                throw new BusinessException(
                    String.format("商品库存不足: productId=%d, sku=%s, 需要数量=%d",
                        item.getProductId(), item.getSku(), item.getQuantity())
                );
            }
        }

        BigDecimal totalAmount = request.getItems().stream()
            .map(item -> item.getTotalAmount() != null ? item.getTotalAmount() :
                (item.getUnitPrice() != null ? item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())) : BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setShopId(request.getShopId());
        order.setPlatform(request.getPlatform());
        order.setStatus(OrderStatus.PENDING.getCode());
        order.setTotalAmount(totalAmount);
        order.setPaidAmount(totalAmount);
        order.setFreightAmount(request.getFreightAmount() != null ? request.getFreightAmount() : BigDecimal.ZERO);
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setBuyerRemark(request.getBuyerRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setDeleted(0);

        orderMapper.insert(order);

        for (OrderItemDTO item : request.getItems()) {
            Boolean reserved = inventoryServiceClient.reserveStock(
                item.getProductId(),
                item.getSku(),
                item.getQuantity(),
                order.getId()
            );
            if (reserved == null || !reserved) {
                throw new BusinessException(
                    String.format("预留库存失败: productId=%d, sku=%s", item.getProductId(), item.getSku())
                );
            }
        }

        log.info("订单创建成功(含库存预留), orderId={}, orderNo={}, itemsCount={}",
            order.getId(), order.getOrderNo(), request.getItems().size());
        return order.getId();
    }

    /**
     * 更新订单状态（带库存商品信息）
     *
     * @param id     订单ID
     * @param status 新状态
     * @param remark 备注
     * @param items  订单商品项（用于库存操作）
     */
    @AuditLog(action = "库存扣减", module = "订单", description = "订单状态变更含库存操作")
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusWithInventory(Long id, String status, String remark, List<OrderItemDTO> items) {
        Order order = getOrderById(id);
        
        validateStatusTransition(order.getStatus(), status);
        
        if (items != null && !items.isEmpty()) {
            handleInventoryOperationWithItems(order, status, items);
        }
        
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Order::getId, id);
        updateWrapper.set(Order::getStatus, status);
        updateWrapper.set(Order::getUpdatedAt, LocalDateTime.now());
        
        if (OrderStatus.PAID.getCode().equals(status)) {
            updateWrapper.set(Order::getPaidAt, LocalDateTime.now());
        } else if (OrderStatus.SHIPPED.getCode().equals(status)) {
            updateWrapper.set(Order::getShippedAt, LocalDateTime.now());
        } else if (OrderStatus.COMPLETED.getCode().equals(status)) {
            updateWrapper.set(Order::getCompletedAt, LocalDateTime.now());
        }
        
        if (StringUtils.hasText(remark)) {
            updateWrapper.set(Order::getSellerRemark, remark);
        }
        
        int rows = orderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("更新订单状态失败");
        }
        
        log.info("订单状态更新成功(含库存操作), orderId={}, oldStatus={}, newStatus={}", id, order.getStatus(), status);
    }

    /**
     * 处理库存操作（带商品明细）
     */
    private void handleInventoryOperationWithItems(Order order, String newStatus, List<OrderItemDTO> items) {
        if (OrderStatus.PAID.getCode().equals(newStatus)) {
            log.info("订单支付成功，开始扣减库存, orderId={}", order.getId());
            for (OrderItemDTO item : items) {
                Boolean deducted = inventoryServiceClient.deductStock(
                    item.getProductId(),
                    item.getSku(),
                    item.getQuantity()
                );
                if (deducted == null || !deducted) {
                    log.error("扣减库存失败: productId={}, sku={}", item.getProductId(), item.getSku());
                    throw new BusinessException("扣减库存失败，请检查库存状态");
                }
            }
        } else if (OrderStatus.CANCELLED.getCode().equals(newStatus)) {
            log.info("订单取消，开始释放预留库存, orderId={}", order.getId());
            for (OrderItemDTO item : items) {
                Boolean released = inventoryServiceClient.releaseStock(
                    item.getProductId(),
                    item.getSku(),
                    item.getQuantity(),
                    order.getId()
                );
                if (released == null || !released) {
                    log.error("释放库存失败: productId={}, sku={}", item.getProductId(), item.getSku());
                    throw new BusinessException("释放库存失败");
                }
            }
        }
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Map<String, Object> getOrderTrend(String range) {
        int days = "month".equals(range) ? 30 : 7;
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> salesAmounts = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            dates.add(date.format(formatter));
            
            LocalDateTime dayStart = date.toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Order::getCreatedAt, dayStart);
            wrapper.lt(Order::getCreatedAt, dayEnd);
            wrapper.in(Order::getStatus, List.of("PAID", "SHIPPED", "DELIVERED", "COMPLETED"));
            
            List<Order> dayOrders = orderMapper.selectList(wrapper);
            orderCounts.add(dayOrders.size());
            
            BigDecimal dayAmount = dayOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            salesAmounts.add(dayAmount);
        }
        
        return Map.of(
            "dates", dates,
            "orderCounts", orderCounts,
            "salesAmounts", salesAmounts
        );
    }

    public List<Map<String, Object>> getPlatformDistribution() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Order::getStatus, List.of("PAID", "SHIPPED", "DELIVERED", "COMPLETED"));
        
        List<Order> orders = orderMapper.selectList(wrapper);
        
        Map<String, Long> platformCount = orders.stream()
            .filter(o -> o.getPlatform() != null)
            .collect(Collectors.groupingBy(Order::getPlatform, Collectors.counting()));
        
        return platformCount.entrySet().stream()
            .map(entry -> Map.<String, Object>of(
                "name", entry.getKey(),
                "value", entry.getValue()
            ))
            .collect(Collectors.toList());
    }
}