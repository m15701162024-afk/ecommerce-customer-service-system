package com.ecommerce.aftersale.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_record")
public class PaymentRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流水号
     */
    private String transactionNo;

    /**
     * 原支付流水号(退款时关联的原支付流水)
     */
    private String originalTransactionNo;

    /**
     * 售后单ID
     */
    private Long afterSaleId;

    /**
     * 订单ID
     */
    private Long orderId;

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
     * 交易类型: payment-支付, refund-退款
     */
    private String transactionType;

    /**
     * 支付渠道: alipay-支付宝, wechat-微信, balance-余额
     */
    private String paymentChannel;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易状态: pending-处理中, success-成功, failed-失败
     */
    private String status;

    /**
     * 第三方交易号
     */
    private String thirdPartyTransactionNo;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 回调通知URL
     */
    private String notifyUrl;

    /**
     * 扩展信息(JSON格式)
     */
    private String extraInfo;

    /**
     * 备注
     */
    private String remark;
}