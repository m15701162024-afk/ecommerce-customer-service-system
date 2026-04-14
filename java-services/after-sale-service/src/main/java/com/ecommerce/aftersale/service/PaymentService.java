package com.ecommerce.aftersale.service;

import com.ecommerce.aftersale.entity.PaymentRecord;

import java.math.BigDecimal;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 执行退款
     *
     * @param afterSaleId 售后单ID
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款流水记录
     */
    PaymentRecord executeRefund(Long afterSaleId, Long orderId, String orderNo, 
                                Long userId, Long shopId, BigDecimal refundAmount, 
                                String refundReason);

    /**
     * 查询退款状态
     *
     * @param transactionNo 流水号
     * @return 退款记录
     */
    PaymentRecord queryRefundStatus(String transactionNo);

    /**
     * 根据售后单ID查询退款记录
     *
     * @param afterSaleId 售后单ID
     * @return 退款记录列表
     */
    java.util.List<PaymentRecord> getRefundRecordsByAfterSaleId(Long afterSaleId);
}