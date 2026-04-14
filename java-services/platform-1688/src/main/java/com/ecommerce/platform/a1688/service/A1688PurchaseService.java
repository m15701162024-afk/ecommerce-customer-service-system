package com.ecommerce.platform.a1688.service;

import com.ecommerce.common.repository.ShopRepository;
import com.ecommerce.platform.a1688.client.A1688ApiClient;
import com.ecommerce.platform.a1688.dto.request.OrderQueryRequest;
import com.ecommerce.platform.a1688.dto.request.PurchaseOrderRequest;
import com.ecommerce.platform.a1688.dto.response.BaseResponse;
import com.ecommerce.platform.a1688.dto.response.OrderDetailResponse;
import com.ecommerce.platform.a1688.dto.response.OrderListResponse;
import com.ecommerce.platform.a1688.dto.response.PurchaseOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 1688采购订单服务
 * 
 * 功能：
 * - 采购订单创建
 * - 订单查询
 * - 订单取消
 * - 订单状态同步
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class A1688PurchaseService {
    
    private final A1688ApiClient apiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ShopRepository shopRepository;
    
    private static final String ORDER_TOPIC = "ecommerce-order-events";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // ==================== 订单创建 ====================
    
    /**
     * 创建采购订单
     * 
     * @param shopId 店铺ID
     * @param request 订单请求
     * @return 订单响应
     */
    public PurchaseOrderResponse createOrder(Long shopId, PurchaseOrderRequest request) {
        log.info("创建1688采购订单: shopId={}, productId={}", shopId, request.getProductId());
        
        // 参数校验
        validateOrderRequest(request);
        
        // 调用API创建订单
        PurchaseOrderResponse response = apiClient.createPurchaseOrder(shopId, request);
        
        if (response != null && response.isSuccess()) {
            // 发布订单创建事件
            OrderEvent event = OrderEvent.builder()
                    .platform("A1688")
                    .shopId(shopId)
                    .orderId(response.getOrderId())
                    .outOrderId(request.getOutOrderId())
                    .eventType("ORDER_CREATED")
                    .totalAmount(response.getTotalAmount())
                    .status(response.getOrderStatus())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(ORDER_TOPIC, response.getOrderId(), event);
            
            log.info("采购订单创建成功: orderId={}, totalAmount={}", 
                    response.getOrderId(), response.getTotalAmount());
        }
        
        return response;
    }
    
    /**
     * 参数校验
     */
    private void validateOrderRequest(PurchaseOrderRequest request) {
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("订单明细不能为空");
        }
        
        for (PurchaseOrderRequest.OrderItem item : request.getOrderItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品数量必须大于0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("商品单价必须大于0");
            }
        }
        
        if (request.getShippingAddress() == null) {
            throw new IllegalArgumentException("收货地址不能为空");
        }
    }
    
    // ==================== 订单查询 ====================
    
    /**
     * 获取订单详情
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    public OrderDetailResponse getOrderDetail(Long shopId, String orderId) {
        log.info("获取1688订单详情: shopId={}, orderId={}", shopId, orderId);
        
        return apiClient.getOrderDetail(shopId, orderId);
    }
    
    /**
     * 查询订单列表
     * 
     * @param shopId 店铺ID
     * @param request 查询请求
     * @return 订单列表
     */
    public OrderListResponse queryOrderList(Long shopId, OrderQueryRequest request) {
        log.info("查询1688订单列表: shopId={}, page={}", shopId, request.getPageNo());
        
        return apiClient.queryOrderList(shopId, request);
    }
    
    /**
     * 根据状态查询订单
     * 
     * @param shopId 店铺ID
     * @param status 订单状态
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    public OrderListResponse queryOrdersByStatus(Long shopId, String status, int pageNo, int pageSize) {
        OrderQueryRequest request = OrderQueryRequest.builder()
                .orderStatus(status)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortField("create_time")
                .sortOrder("desc")
                .build();
        
        return queryOrderList(shopId, request);
    }
    
    // ==================== 订单操作 ====================
    
    /**
     * 取消订单
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 操作结果
     */
    public BaseResponse cancelOrder(Long shopId, String orderId, String reason) {
        log.info("取消1688订单: shopId={}, orderId={}, reason={}", shopId, orderId, reason);
        
        BaseResponse response = apiClient.cancelOrder(shopId, orderId, reason);
        
        if (response != null && response.isSuccess()) {
            // 发布订单取消事件
            OrderEvent event = OrderEvent.builder()
                    .platform("A1688")
                    .shopId(shopId)
                    .orderId(orderId)
                    .eventType("ORDER_CANCELLED")
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(ORDER_TOPIC, orderId, event);
            
            log.info("订单取消成功: orderId={}", orderId);
        }
        
        return response;
    }
    
    /**
     * 确认收货
     * 
     * @param shopId 店铺ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    public BaseResponse confirmReceive(Long shopId, String orderId) {
        log.info("确认收货: shopId={}, orderId={}", shopId, orderId);
        
        BaseResponse response = apiClient.confirmReceive(shopId, orderId);
        
        if (response != null && response.isSuccess()) {
            // 发布订单完成事件
            OrderEvent event = OrderEvent.builder()
                    .platform("A1688")
                    .shopId(shopId)
                    .orderId(orderId)
                    .eventType("ORDER_COMPLETED")
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(ORDER_TOPIC, orderId, event);
            
            log.info("确认收货成功: orderId={}", orderId);
        }
        
        return response;
    }
    
    // ==================== 订单同步 ====================
    
    /**
     * 定时同步订单状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRateString = "300000")
    public void syncOrderStatus() {
        log.info("开始同步1688订单状态...");
        
        List<Long> activeShops = getActiveShops();
        
        for (Long shopId : activeShops) {
            try {
                syncShopOrders(shopId);
            } catch (Exception e) {
                log.error("同步店铺订单失败: shopId={}", shopId, e);
            }
        }
        
        log.info("订单状态同步完成");
    }
    
    /**
     * 同步店铺订单
     */
    private void syncShopOrders(Long shopId) {
        String endTime = LocalDateTime.now().format(FORMATTER);
        String startTime = LocalDateTime.now().minusMinutes(5).format(FORMATTER);
        
        OrderQueryRequest request = OrderQueryRequest.builder()
                .startTime(LocalDateTime.parse(startTime, FORMATTER))
                .endTime(LocalDateTime.parse(endTime, FORMATTER))
                .pageNo(1)
                .pageSize(50)
                .build();
        
        OrderListResponse response = apiClient.queryOrderList(shopId, request);
        
        if (response != null && response.getOrders() != null) {
            for (OrderDetailResponse order : response.getOrders()) {
                processOrderUpdate(shopId, order);
            }
        }
    }
    
    /**
     * 处理订单更新
     */
    private void processOrderUpdate(Long shopId, OrderDetailResponse order) {
        OrderEvent event = OrderEvent.builder()
                .platform("A1688")
                .shopId(shopId)
                .orderId(order.getOrderId())
                .outOrderId(order.getOutOrderId())
                .eventType("ORDER_SYNCED")
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .timestamp(System.currentTimeMillis())
                .build();
        
        kafkaTemplate.send(ORDER_TOPIC, order.getOrderId(), event);
        
        log.debug("订单同步: orderId={}, status={}", order.getOrderId(), order.getStatus());
    }
    
    /**
     * 获取活跃店铺列表
     */
    private List<Long> getActiveShops() {
        return shopRepository.findIdsByPlatformAndStatus("A1688", 1);
    }
    
    // ==================== 事件定义 ====================
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderEvent {
        private String platform;
        private Long shopId;
        private String orderId;
        private String outOrderId;
        private String eventType;
        private String status;
        private Long totalAmount;
        private Long timestamp;
    }
}