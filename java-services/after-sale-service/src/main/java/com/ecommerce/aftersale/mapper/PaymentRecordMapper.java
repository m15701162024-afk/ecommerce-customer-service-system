package com.ecommerce.aftersale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.aftersale.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 支付流水Mapper
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    /**
     * 根据售后单ID查询退款记录
     */
    @Select("SELECT * FROM t_payment_record WHERE after_sale_id = #{afterSaleId} AND transaction_type = 'refund' ORDER BY created_at DESC")
    List<PaymentRecord> selectByAfterSaleId(@Param("afterSaleId") Long afterSaleId);

    /**
     * 根据订单ID查询支付记录
     */
    @Select("SELECT * FROM t_payment_record WHERE order_id = #{orderId} AND transaction_type = 'payment' AND status = 'success' ORDER BY created_at DESC LIMIT 1")
    PaymentRecord selectSuccessfulPaymentByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据流水号查询
     */
    @Select("SELECT * FROM t_payment_record WHERE transaction_no = #{transactionNo}")
    PaymentRecord selectByTransactionNo(@Param("transactionNo") String transactionNo);
}