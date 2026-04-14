package com.ecommerce.platform.a1688.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 采购订单响应DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    
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
     */
    private String orderStatus;
    
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
     * 支付URL
     */
    private String payUrl;
    
    /**
     * 支付截止时间
     */
    private Long payExpireTime;
    
    /**
     * 错误码
     */
    private Integer errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return orderId != null && (errorCode == null || errorCode == 0);
    }
}