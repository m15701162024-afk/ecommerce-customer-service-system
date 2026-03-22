package com.ecommerce.platform.douyin.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderDetailResponse {
    private Integer errNo;
    private String errMsg;
    private Data data;
    
    @lombok.Data
    public static class Data {
        private String shopOrderId;
        private String orderId;
        private List<ProductItem> productInfo;
        private PostInfo postInfo;
        private PayInfo payInfo;
        private String createTime;
        private String payTime;
        private String updateTime;
        private Integer status;
        private Integer afterSaleStatus;
    }
    
    @lombok.Data
    public static class ProductItem {
        private String productId;
        private String skuId;
        private String productName;
        private String productImg;
        private Integer quantity;
        private Long price;
        private Long payAmount;
    }
    
    @lombok.Data
    public static class PostInfo {
        private String name;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String address;
    }
    
    @lombok.Data
    public static class PayInfo {
        private Long totalAmount;
        private Long payAmount;
        private Long freightAmount;
        private Long discountAmount;
    }
}