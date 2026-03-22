package com.ecommerce.platform.douyin.service;

import com.ecommerce.platform.douyin.client.DouyinApiClient;
import com.ecommerce.platform.douyin.dto.OrderDetailResponse;
import com.ecommerce.platform.douyin.dto.OrderListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DouyinOrderSyncService {
    
    private final DouyinApiClient apiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_TOPIC = "ecommerce-order-events";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Scheduled(fixedRate = 60000)
    public void syncOrders() {
        log.info("开始同步抖音订单...");
        
        String endTime = LocalDateTime.now().format(FORMATTER);
        String startTime = LocalDateTime.now().minusMinutes(5).format(FORMATTER);
        
        List<Long> activeShops = getActiveShops();
        
        for (Long shopId : activeShops) {
            try {
                syncShopOrders(shopId, startTime, endTime);
            } catch (Exception e) {
                log.error("同步店铺订单失败: shopId={}", shopId, e);
            }
        }
    }
    
    private void syncShopOrders(Long shopId, String startTime, String endTime) {
        int page = 1;
        int size = 50;
        boolean hasMore = true;
        
        while (hasMore) {
            OrderListResponse response = apiClient.getOrderList(shopId, page, size, startTime, endTime);
            
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
            
            hasMore = orders.size() >= size;
            page++;
        }
    }
    
    private void processOrder(Long shopId, OrderListResponse.OrderInfo order) {
        OrderDetailResponse detail = apiClient.getOrderDetail(shopId, order.getShopOrderId());
        
        if (detail != null && detail.getData() != null) {
            OrderEvent event = convertToEvent(shopId, detail.getData());
            
            kafkaTemplate.send(ORDER_TOPIC, event.getOrderNo(), event);
            log.info("订单同步成功: orderNo={}, status={}", event.getOrderNo(), event.getStatus());
        }
    }
    
    private OrderEvent convertToEvent(Long shopId, OrderDetailResponse.Data data) {
        OrderEvent event = new OrderEvent();
        event.setPlatform("DOUYIN");
        event.setShopId(shopId);
        event.setOrderNo(data.getShopOrderId());
        event.setPlatformOrderId(data.getOrderId());
        event.setStatus(mapStatus(data.getStatus()));
        event.setTotalAmount(data.getPayInfo().getTotalAmount());
        event.setPayAmount(data.getPayInfo().getPayAmount());
        event.setCreateTime(data.getCreateTime());
        event.setPayTime(data.getPayTime());
        return event;
    }
    
    private String mapStatus(Integer status) {
        return switch (status) {
            case 1 -> "PENDING";
            case 2 -> "PAID";
            case 3 -> "SHIPPED";
            case 4 -> "COMPLETED";
            case 5 -> "CANCELLED";
            default -> "UNKNOWN";
        };
    }
    
    private List<Long> getActiveShops() {
        return List.of(1L);
    }
    
    @lombok.Data
    public static class OrderEvent {
        private String platform;
        private Long shopId;
        private String orderNo;
        private String platformOrderId;
        private String status;
        private Long totalAmount;
        private Long payAmount;
        private String createTime;
        private String payTime;
    }
}