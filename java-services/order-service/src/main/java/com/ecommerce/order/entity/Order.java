package com.ecommerce.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

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
     * 实付金额
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
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 买家备注
     */
    private String buyerRemark;

    /**
     * 卖家备注
     */
    private String sellerRemark;

    /**
     * 支付时间
     */
    private java.time.LocalDateTime paidAt;

    /**
     * 发货时间
     */
    private java.time.LocalDateTime shippedAt;

    /**
     * 完成时间
     */
    private java.time.LocalDateTime completedAt;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String logisticsNo;
}