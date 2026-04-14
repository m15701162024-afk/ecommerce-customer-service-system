package com.ecommerce.platform.a1688.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单详情响应DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    
    /**
     * 订单ID
     */
    private String orderId;
    
    /**
     * 外部订单号
     */
    private String outOrderId;
    
    /**
     * 订单状态
     * wait_pay: 待付款
     * wait_send: 待发货
     * wait_receive: 待收货
     * finished: 已完成
     * cancelled: 已取消
     * refunding: 退款中
     */
    private String status;
    
    /**
     * 订单状态描述
     */
    private String statusDesc;
    
    /**
     * 买家会员ID
     */
    private String buyerMemberId;
    
    /**
     * 买家名称
     */
    private String buyerName;
    
    /**
     * 供应商会员ID
     */
    private String sellerMemberId;
    
    /**
     * 供应商名称
     */
    private String sellerName;
    
    /**
     * 订单商品列表
     */
    private List<OrderItem> orderItems;
    
    /**
     * 订单金额（分）
     */
    private Long totalAmount;
    
    /**
     * 实付金额（分）
     */
    private Long payAmount;
    
    /**
     * 运费（分）
     */
    private Long freightAmount;
    
    /**
     * 优惠金额（分）
     */
    private Long discountAmount;
    
    /**
     * 收货地址
     */
    private ShippingInfo shippingInfo;
    
    /**
     * 发票信息
     */
    private InvoiceInfo invoiceInfo;
    
    /**
     * 支付信息
     */
    private PaymentDetail paymentDetail;
    
    /**
     * 物流信息
     */
    private LogisticsInfo logisticsInfo;
    
    /**
     * 买家留言
     */
    private String buyerMessage;
    
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
    private Long deliverTime;
    
    /**
     * 完成时间
     */
    private Long finishTime;
    
    /**
     * 取消时间
     */
    private Long cancelTime;
    
    /**
     * 错误码
     */
    private Integer errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 订单商品
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        
        /**
         * 商品ID
         */
        private String productId;
        
        /**
         * 商品名称
         */
        private String productName;
        
        /**
         * 商品图片
         */
        private String productImage;
        
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * SKU规格
         */
        private String skuSpec;
        
        /**
         * 数量
         */
        private Integer quantity;
        
        /**
         * 单价（分）
         */
        private Long unitPrice;
        
        /**
         * 小计金额（分）
         */
        private Long subTotal;
        
        /**
         * 优惠金额（分）
         */
        private Long discountAmount;
        
        /**
         * 实付金额（分）
         */
        private Long payAmount;
    }
    
    /**
     * 收货信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfo {
        
        /**
         * 收货人姓名
         */
        private String receiverName;
        
        /**
         * 收货人电话
         */
        private String receiverPhone;
        
        /**
         * 省份
         */
        private String province;
        
        /**
         * 城市
         */
        private String city;
        
        /**
         * 区/县
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
    
    /**
     * 发票信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceInfo {
        
        /**
         * 发票类型
         */
        private String invoiceType;
        
        /**
         * 发票抬头
         */
        private String invoiceTitle;
        
        /**
         * 纳税人识别号
         */
        private String taxNumber;
    }
    
    /**
     * 支付详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetail {
        
        /**
         * 支付方式
         */
        private String paymentMethod;
        
        /**
         * 支付流水号
         */
        private String paymentNo;
        
        /**
         * 支付状态
         */
        private String paymentStatus;
        
        /**
         * 支付时间
         */
        private Long paymentTime;
    }
    
    /**
     * 物流信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
         * 物流状态
         */
        private String logisticsStatus;
        
        /**
         * 物流轨迹
         */
        private List<LogisticsTrace> traces;
    }
    
    /**
     * 物流轨迹
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticsTrace {
        
        /**
         * 时间
         */
        private Long time;
        
        /**
         * 状态描述
         */
        private String statusDesc;
        
        /**
         * 详情
         */
        private String detail;
    }
}