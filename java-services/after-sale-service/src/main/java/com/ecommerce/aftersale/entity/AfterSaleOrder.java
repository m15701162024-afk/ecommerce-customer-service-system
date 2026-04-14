package com.ecommerce.aftersale.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后订单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_after_sale_order")
public class AfterSaleOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 售后单号
     */
    private String afterSaleNo;

    /**
     * 原订单ID
     */
    private Long orderId;

    /**
     * 原订单编号
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
     * 售后类型: refund_only-仅退款, return_refund-退货退款, exchange-换货
     */
    private String type;

    /**
     * 售后状态
     */
    private String status;

    /**
     * 售后原因ID
     */
    private Long reasonId;

    /**
     * 售后原因描述
     */
    private String reasonDesc;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 凭证图片(JSON数组)
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private java.util.List<String> evidenceImages;

    /**
     * 退货物流公司
     */
    private String returnLogisticsCompany;

    /**
     * 退货物流单号
     */
    private String returnLogisticsNo;

    /**
     * 退货时间
     */
    private LocalDateTime returnedAt;

    /**
     * 退货地址ID
     */
    private Long returnAddressId;

    /**
     * 换货商品SKU
     */
    private String exchangeSku;

    /**
     * 换货数量
     */
    private Integer exchangeQuantity;

    /**
     * 换货发货物流公司
     */
    private String exchangeLogisticsCompany;

    /**
     * 换货发货物流单号
     */
    private String exchangeLogisticsNo;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;

    /**
     * 审批备注
     */
    private String approveRemark;

    /**
     * 退款时间
     */
    private LocalDateTime refundedAt;

    /**
     * 退款流水号
     */
    private String refundTransactionNo;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 买家备注
     */
    private String buyerRemark;

    /**
     * 卖家备注
     */
    private String sellerRemark;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品SKU
     */
    private String productSku;

    /**
     * 商品数量
     */
    private Integer quantity;
}