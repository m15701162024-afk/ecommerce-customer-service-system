package com.ecommerce.platform.xiaohongshu.service;

import com.ecommerce.common.repository.ShopRepository;
import com.ecommerce.platform.xiaohongshu.client.XiaohongshuApiClient;
import com.ecommerce.platform.xiaohongshu.dto.OrderDetailResponse;
import com.ecommerce.platform.xiaohongshu.dto.OrderListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 小红书订单同步服务
 * 
 * 功能:
 * - 订单列表同步
 * - 订单详情同步
 * - 订单状态变更处理
 * - 定时同步任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XiaohongshuOrderService {

    private final XiaohongshuApiClient apiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ShopRepository shopRepository;

    private static final String ORDER_TOPIC = "ecommerce-order-events";
    private static final String ORDER_STATUS_TOPIC = "ecommerce-order-status";

    /**
     * 定时同步订单 (每5分钟)
     */
    @Scheduled(fixedRate = 300000)
    public void syncOrders() {
        log.info("开始同步小红书订单...");

        List<Long> activeShops = getActiveShops();

        for (Long shopId : activeShops) {
            try {
                syncShopOrders(shopId);
            } catch (Exception e) {
                log.error("同步店铺订单失败: shopId={}", shopId, e);
            }
        }

        log.info("订单同步完成");
    }

    /**
     * 同步店铺订单
     */
    private void syncShopOrders(Long shopId) {
        long endTime = Instant.now().getEpochSecond();
        long startTime = Instant.now().minus(5, ChronoUnit.MINUTES).getEpochSecond();

        int page = 1;
        int pageSize = 50;
        boolean hasMore = true;

        while (hasMore) {
            OrderListResponse response = apiClient.getOrderList(shopId, page, pageSize, startTime, endTime);

            if (response == null || response.getData() == null) {
                break;
            }

            List<OrderListResponse.OrderInfo> orders = response.getData().getList();
            if (orders == null || orders.isEmpty()) {
                break;
            }

            for (OrderListResponse.OrderInfo order : orders) {
                processOrder(shopId, order);
            }

            hasMore = response.getData().getHasMore() != null && response.getData().getHasMore();
            page++;
        }
    }

    /**
     * 处理订单
     */
    private void processOrder(Long shopId, OrderListResponse.OrderInfo order) {
        try {
            // 获取订单详情
            OrderDetailResponse detail = apiClient.getOrderDetail(shopId, order.getOrderId());

            if (detail != null && detail.getData() != null) {
                OrderEvent event = convertToEvent(shopId, detail.getData());
                kafkaTemplate.send(ORDER_TOPIC, event.getOrderNo(), event);
                log.info("订单同步成功: orderNo={}, status={}", event.getOrderNo(), event.getStatus());
            }
        } catch (Exception e) {
            log.error("处理订单失败: orderId={}", order.getOrderId(), e);
        }
    }

    /**
     * 获取订单详情
     */
    public OrderDetailResponse getOrderDetail(Long shopId, String orderId) {
        return apiClient.getOrderDetail(shopId, orderId);
    }

    /**
     * 获取订单列表
     */
    public OrderListResponse getOrderList(Long shopId, int page, int pageSize, Long startTime, Long endTime) {
        return apiClient.getOrderList(shopId, page, pageSize, startTime, endTime);
    }

    /**
     * 处理订单状态变更
     */
    public void handleOrderStatusChange(Long shopId, String orderId, Integer newStatus) {
        log.info("处理小红书订单状态变更: shopId={}, orderId={}, newStatus={}", shopId, orderId, newStatus);

        OrderStatusEvent event = new OrderStatusEvent();
        event.setPlatform("XIAOHONGSHU");
        event.setShopId(shopId);
        event.setOrderId(orderId);
        event.setNewStatus(mapStatus(newStatus));
        event.setTimestamp(System.currentTimeMillis());

        kafkaTemplate.send(ORDER_STATUS_TOPIC, orderId, event);
        log.info("订单状态变更事件已发送: orderId={}, status={}", orderId, event.getNewStatus());
    }

    /**
     * 转换为订单事件
     */
    private OrderEvent convertToEvent(Long shopId, OrderDetailResponse.Data data) {
        OrderEvent event = new OrderEvent();
        event.setPlatform("XIAOHONGSHU");
        event.setShopId(shopId);
        event.setOrderNo(data.getShopOrderId());
        event.setPlatformOrderId(data.getOrderId());
        event.setStatus(mapStatus(data.getStatus()));
        
        if (data.getPayInfo() != null) {
            event.setTotalAmount(data.getPayInfo().getTotalAmount());
            event.setPayAmount(data.getPayInfo().getPayAmount());
            event.setFreightAmount(data.getPayInfo().getFreightAmount());
            event.setDiscountAmount(data.getPayInfo().getDiscountAmount());
        }
        
        if (data.getReceiverInfo() != null) {
            event.setReceiverName(data.getReceiverInfo().getName());
            event.setReceiverPhone(data.getReceiverInfo().getPhone());
            event.setReceiverAddress(buildFullAddress(data.getReceiverInfo()));
        }
        
        event.setCreateTime(data.getCreateTime());
        event.setPayTime(data.getPayTime());
        event.setShipTime(data.getShipTime());
        event.setFinishTime(data.getFinishTime());
        event.setBuyerRemark(data.getBuyerRemark());
        
        return event;
    }

    /**
     * 构建完整地址
     */
    private String buildFullAddress(OrderDetailResponse.ReceiverInfo receiverInfo) {
        StringBuilder sb = new StringBuilder();
        if (receiverInfo.getProvince() != null) {
            sb.append(receiverInfo.getProvince());
        }
        if (receiverInfo.getCity() != null) {
            sb.append(receiverInfo.getCity());
        }
        if (receiverInfo.getDistrict() != null) {
            sb.append(receiverInfo.getDistrict());
        }
        if (receiverInfo.getAddress() != null) {
            sb.append(receiverInfo.getAddress());
        }
        return sb.toString();
    }

    /**
     * 映射订单状态
     */
    private String mapStatus(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        return switch (status) {
            case 1 -> "PENDING";        // 待付款
            case 2 -> "PAID";           // 已付款
            case 3 -> "SHIPPED";        // 已发货
            case 4 -> "COMPLETED";      // 已完成
            case 5 -> "CANCELLED";      // 已取消
            case 6 -> "REFUNDING";      // 退款中
            case 7 -> "REFUNDED";       // 已退款
            default -> "UNKNOWN";
        };
    }

    /**
     * 获取活跃店铺列表
     */
    private List<Long> getActiveShops() {
        return shopRepository.findIdsByPlatformAndStatus("XIAOHONGSHU", 1);
    }

    /**
     * 订单事件
     */
    @lombok.Data
    public static class OrderEvent {
        private String platform;
        private Long shopId;
        private String orderNo;
        private String platformOrderId;
        private String status;
        private Long totalAmount;
        private Long payAmount;
        private Long freightAmount;
        private Long discountAmount;
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;
        private Long createTime;
        private Long payTime;
        private Long shipTime;
        private Long finishTime;
        private String buyerRemark;
    }

    /**
     * 订单状态事件
     */
    @lombok.Data
    public static class OrderStatusEvent {
        private String platform;
        private Long shopId;
        private String orderId;
        private String newStatus;
        private Long timestamp;
    }
}