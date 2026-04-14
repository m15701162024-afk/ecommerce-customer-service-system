package com.ecommerce.aftersale.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单DTO - 用于Feign调用订单服务返回的数据
 */
@Data
public class OrderDTO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 平台来源
     */
    private String platform;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额 - 用于退款金额上限验证
     */
    private BigDecimal paidAmount;

    /**
     * 运费
     */
    private BigDecimal freightAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 发货时间
     */
    private LocalDateTime shippedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}