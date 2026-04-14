package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;
import java.util.List;

/**
 * 小红书订单详情响应
 */
@Data
public class OrderDetailResponse {
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
         * 订单ID
         */
        private String orderId;
        
        /**
         * 店铺订单ID
         */
        private String shopOrderId;
        
        /**
         * 订单状态
         */
        private Integer status;
        
        /**
         * 售后状态
         */
        private Integer afterSaleStatus;
        
        /**
         * 商品信息
         */
        private List<ProductItem> productInfo;
        
        /**
         * 收货信息
         */
        private ReceiverInfo receiverInfo;
        
        /**
         * 支付信息
         */
        private PayInfo payInfo;
        
        /**
         * 物流信息
         */
        private LogisticsInfo logisticsInfo;
        
        /**
         * 创建时间
         */
        private Long createTime;
        
        /**
         * 支付时间
         */
        private Long payTime;
        
        /**
         * 发货时间
         */
        private Long shipTime;
        
        /**
         * 完成时间
         */
        private Long finishTime;
        
        /**
         * 更新时间
         */
        private Long updateTime;
        
        /**
         * 买家备注
         */
        private String buyerRemark;
        
        /**
         * 卖家备注
         */
        private String sellerRemark;
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
        
        /**
         * 商品规格
         */
        private String specInfo;
    }
    
    @Data
    public static class ReceiverInfo {
        /**
         * 收货人姓名
         */
        private String name;
        
        /**
         * 收货人电话
         */
        private String phone;
        
        /**
         * 省
         */
        private String province;
        
        /**
         * 市
         */
        private String city;
        
        /**
         * 区
         */
        private String district;
        
        /**
         * 详细地址
         */
        private String address;
        
        /**
         * 邮编
         */
        private String zipCode;
    }
    
    @Data
    public static class PayInfo {
        /**
         * 订单总金额(分)
         */
        private Long totalAmount;
        
        /**
         * 实付金额(分)
         */
        private Long payAmount;
        
        /**
         * 运费(分)
         */
        private Long freightAmount;
        
        /**
         * 优惠金额(分)
         */
        private Long discountAmount;
        
        /**
         * 支付方式
         */
        private Integer payType;
        
        /**
         * 支付流水号
         */
        private String payTransactionId;
    }
    
    @Data
    public static class LogisticsInfo {
        /**
         * 物流公司
         */
        private String logisticsCompany;
        
        /**
         * 物流单号
         */
        private String logisticsNo;
        
        /**
         * 发货时间
         */
        private Long shipTime;
    }
}