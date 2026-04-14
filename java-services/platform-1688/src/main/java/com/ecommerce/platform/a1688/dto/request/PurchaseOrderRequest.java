package com.ecommerce.platform.a1688.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 采购订单创建请求DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {
    
    /**
     * 商品ID
     */
    @NotBlank(message = "商品ID不能为空")
    private String productId;
    
    /**
     * SKU ID
     */
    private String skuId;
    
    /**
     * 供应商会员ID
     */
    @NotBlank(message = "供应商ID不能为空")
    private String sellerMemberId;
    
    /**
     * 订单明细列表
     */
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItem> orderItems;
    
    /**
     * 收货地址信息
     */
    @NotNull(message = "收货地址不能为空")
    private ShippingAddress shippingAddress;
    
    /**
     * 发票信息
     */
    private InvoiceInfo invoiceInfo;
    
    /**
     * 买家留言
     */
    @Size(max = 500, message = "留言长度不能超过500字符")
    private String buyerMessage;
    
    /**
     * 外部订单号（用于幂等性校验）
     */
    private String outOrderId;
    
    /**
     * 订单类型
     * normal: 普通订单
     * sample: 样品订单
     * pre_sale: 预售订单
     */
    @Builder.Default
    private String orderType = "normal";
    
    /**
     * 订单明细
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        
        /**
         * SKU ID
         */
        @NotBlank(message = "SKU ID不能为空")
        private String skuId;
        
        /**
         * 商品名称
         */
        private String productName;
        
        /**
         * SKU规格
         */
        private String skuSpec;
        
        /**
         * 数量
         */
        @Min(value = 1, message = "数量最小为1")
        private Integer quantity;
        
        /**
         * 单价（分）
         */
        @Min(value = 1, message = "单价必须大于0")
        private Long unitPrice;
    }
    
    /**
     * 收货地址
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        
        /**
         * 收货人姓名
         */
        @NotBlank(message = "收货人姓名不能为空")
        private String receiverName;
        
        /**
         * 收货人电话
         */
        @NotBlank(message = "收货人电话不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String receiverPhone;
        
        /**
         * 省份
         */
        @NotBlank(message = "省份不能为空")
        private String province;
        
        /**
         * 城市
         */
        @NotBlank(message = "城市不能为空")
        private String city;
        
        /**
         * 区/县
         */
        @NotBlank(message = "区/县不能为空")
        private String district;
        
        /**
         * 详细地址
         */
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 200, message = "地址长度不能超过200字符")
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
         * normal: 普通发票
         * special: 增值税专用发票
         */
        private String invoiceType;
        
        /**
         * 发票抬头
         */
        @NotBlank(message = "发票抬头不能为空")
        private String invoiceTitle;
        
        /**
         * 纳税人识别号
         */
        @NotBlank(message = "纳税人识别号不能为空")
        private String taxNumber;
        
        /**
         * 注册地址
         */
        private String registerAddress;
        
        /**
         * 注册电话
         */
        private String registerPhone;
        
        /**
         * 开户银行
         */
        private String bankName;
        
        /**
         * 银行账号
         */
        private String bankAccount;
    }
}