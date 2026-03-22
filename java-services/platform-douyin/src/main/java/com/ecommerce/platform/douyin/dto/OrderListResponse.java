package com.ecommerce.platform.douyin.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderListResponse {
    private Integer errNo;
    private String errMsg;
    private Data data;
    
    @lombok.Data
    public static class Data {
        private List<OrderInfo> list;
        private Long total;
        private Integer page;
        private Integer size;
    }
    
    @lombok.Data
    public static class OrderInfo {
        private String shopOrderId;
        private String orderId;
        private String productInfo;
        private Long payAmount;
        private Integer payType;
        private Integer status;
        private String createTime;
        private String payTime;
        private String buyerInfo;
    }
}