package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;
import java.util.List;

/**
 * 小红书订单列表响应
 */
@Data
public class OrderListResponse {
    /**
     * 错误码，0表示成功
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private Data data;
    
    @Data
    public static class Data {
        /**
         * 订单列表
         */
        private List<OrderInfo> list;
        
        /**
         * 总数
         */
        private Long total;
        
        /**
         * 当前页码
         */
        private Integer page;
        
        /**
         * 每页数量
         */
        private Integer pageSize;
        
        /**
         * 是否有更多
         */
        private Boolean hasMore;
    }
    
    @Data
    public static class OrderInfo {
        /**
         * 订单ID
         */
        private String orderId;
        
        /**
         * 店铺订单ID
         */
        private String shopOrderId;
        
        /**
         * 买家信息
         */
        private BuyerInfo buyerInfo;
        
        /**
         * 订单状态
         */
        private Integer status;
        
        /**
         * 支付金额(分)
         */
        private Long payAmount;
        
        /**
         * 商品信息
         */
        private List<ProductItem> productInfo;
        
        /**
         * 创建时间
         */
        private Long createTime;
        
        /**
         * 支付时间
         */
        private Long payTime;
        
        /**
         * 更新时间
         */
        private Long updateTime;
    }
    
    @Data
    public static class BuyerInfo {
        /**
         * 买家ID
         */
        private String buyerId;
        
        /**
         * 买家昵称
         */
        private String nickname;
        
        /**
         * 买家头像
         */
        private String avatar;
    }
    
    @Data
    public static class ProductItem {
        /**
         * 商品ID
         */
        private String productId;
        
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * 商品名称
         */
        private String productName;
        
        /**
         * 商品图片
         */
        private String productImg;
        
        /**
         * 数量
         */
        private Integer quantity;
        
        /**
         * 单价(分)
         */
        private Long price;
        
        /**
         * 实付金额(分)
         */
        private Long payAmount;
    }
}