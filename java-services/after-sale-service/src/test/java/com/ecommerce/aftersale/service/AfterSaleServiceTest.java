package com.ecommerce.aftersale.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.aftersale.dto.request.*;
import com.ecommerce.aftersale.entity.AfterSaleOrder;
import com.ecommerce.aftersale.entity.AfterSaleReason;
import com.ecommerce.aftersale.enums.AfterSaleStatus;
import com.ecommerce.aftersale.enums.AfterSaleType;
import com.ecommerce.aftersale.mapper.AfterSaleOrderMapper;
import com.ecommerce.aftersale.mapper.AfterSaleReasonMapper;
import com.ecommerce.aftersale.mapper.ReturnAddressMapper;
import com.ecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 售后服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AfterSaleServiceTest {

    @Mock
    private AfterSaleOrderMapper afterSaleOrderMapper;

    @Mock
    private AfterSaleReasonMapper afterSaleReasonMapper;

    @Mock
    private ReturnAddressMapper returnAddressMapper;

    @InjectMocks
    private AfterSaleService afterSaleService;

    private AfterSaleReason testReason;
    private ApplyAfterSaleRequest applyRequest;

    @BeforeEach
    void setUp() {
        testReason = new AfterSaleReason();
        testReason.setId(1L);
        testReason.setType("REFUND_ONLY");
        testReason.setReasonCode("NOT_NEEDED");
        testReason.setReasonText("不想要了");
        testReason.setEnabled(true);

        applyRequest = new ApplyAfterSaleRequest();
        applyRequest.setOrderId(1L);
        applyRequest.setType("REFUND_ONLY");
        applyRequest.setReasonId(1L);
        applyRequest.setReason("不想要了");
        applyRequest.setRefundAmount(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("申请售后 - 仅退款成功")
    void applyAfterSale_RefundOnly_Success() {
        // Given
        when(afterSaleReasonMapper.selectById(1L)).thenReturn(testReason);
        when(afterSaleOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(afterSaleOrderMapper.insert(any(AfterSaleOrder.class))).thenAnswer(invocation -> {
            AfterSaleOrder order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });

        // When
        Long result = afterSaleService.applyAfterSale(applyRequest, 1L, 1L, "DOUYIN");

        // Then
        assertNotNull(result);
        verify(afterSaleOrderMapper, times(1)).insert(any(AfterSaleOrder.class));
    }

    @Test
    @DisplayName("申请售后 - 退货退款成功")
    void applyAfterSale_ReturnRefund_Success() {
        // Given
        testReason.setType("RETURN_REFUND");
        applyRequest.setType("RETURN_REFUND");
        
        when(afterSaleReasonMapper.selectById(1L)).thenReturn(testReason);
        when(afterSaleOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(afterSaleOrderMapper.insert(any(AfterSaleOrder.class))).thenAnswer(invocation -> {
            AfterSaleOrder order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });

        // When
        Long result = afterSaleService.applyAfterSale(applyRequest, 1L, 1L, "DOUYIN");

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("申请售后 - 换货成功")
    void applyAfterSale_Exchange_Success() {
        // Given
        testReason.setType("EXCHANGE");
        applyRequest.setType("EXCHANGE");
        applyRequest.setExchangeSku("SKU-001");
        applyRequest.setExchangeQuantity(1);
        applyRequest.setRefundAmount(null);
        
        when(afterSaleReasonMapper.selectById(1L)).thenReturn(testReason);
        when(afterSaleOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(afterSaleOrderMapper.insert(any(AfterSaleOrder.class))).thenAnswer(invocation -> {
            AfterSaleOrder order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });

        // When
        Long result = afterSaleService.applyAfterSale(applyRequest, 1L, 1L, "DOUYIN");

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("申请售后 - 已存在进行中售后")
    void applyAfterSale_AlreadyExists() {
        // Given
        when(afterSaleReasonMapper.selectById(1L)).thenReturn(testReason);
        when(afterSaleOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            afterSaleService.applyAfterSale(applyRequest, 1L, 1L, "DOUYIN"));
    }

    @Test
    @DisplayName("申请售后 - 原因不存在")
    void applyAfterSale_ReasonNotFound() {
        // Given
        when(afterSaleReasonMapper.selectById(1L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            afterSaleService.applyAfterSale(applyRequest, 1L, 1L, "DOUYIN"));
    }

    @Test
    @DisplayName("审批通过 - 仅退款")
    void approveAfterSale_RefundOnly_Success() {
        // Given
        AfterSaleOrder order = createTestAfterSaleOrder();
        order.setType(AfterSaleType.REFUND_ONLY.getCode());
        order.setStatus(AfterSaleStatus.PENDING.getCode());

        ApproveAfterSaleRequest request = new ApproveAfterSaleRequest();
        request.setAfterSaleId(1L);
        request.setApproved(true);

        when(afterSaleOrderMapper.selectById(1L)).thenReturn(order);
        when(afterSaleOrderMapper.updateById(any(AfterSaleOrder.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            afterSaleService.approveAfterSale(request, 1L, "admin"));
    }

    @Test
    @DisplayName("审批通过 - 退货退款")
    void approveAfterSale_ReturnRefund_Success() {
        // Given
        AfterSaleOrder order = createTestAfterSaleOrder();
        order.setType(AfterSaleType.RETURN_REFUND.getCode());
        order.setStatus(AfterSaleStatus.PENDING.getCode());

        ApproveAfterSaleRequest request = new ApproveAfterSaleRequest();
        request.setAfterSaleId(1L);
        request.setApproved(true);
        request.setReturnAddressId(1L);

        when(afterSaleOrderMapper.selectById(1L)).thenReturn(order);
        when(afterSaleOrderMapper.updateById(any(AfterSaleOrder.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            afterSaleService.approveAfterSale(request, 1L, "admin"));
    }

    @Test
    @DisplayName("审批拒绝")
    void approveAfterSale_Reject() {
        // Given
        AfterSaleOrder order = createTestAfterSaleOrder();
        order.setStatus(AfterSaleStatus.PENDING.getCode());

        ApproveAfterSaleRequest request = new ApproveAfterSaleRequest();
        request.setAfterSaleId(1L);
        request.setApproved(false);
        request.setRejectReason("不符合退款条件");

        when(afterSaleOrderMapper.selectById(1L)).thenReturn(order);
        when(afterSaleOrderMapper.updateById(any(AfterSaleOrder.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            afterSaleService.approveAfterSale(request, 1L, "admin"));
    }

    @Test
    @DisplayName("填写退货物流 - 成功")
    void fillLogistics_Success() {
        // Given
        AfterSaleOrder order = createTestAfterSaleOrder();
        order.setStatus(AfterSaleStatus.APPROVED.getCode());

        FillLogisticsRequest request = new FillLogisticsRequest();
        request.setAfterSaleId(1L);
        request.setLogisticsCompany("顺丰速运");
        request.setLogisticsNo("SF1234567890");

        when(afterSaleOrderMapper.selectById(1L)).thenReturn(order);
        when(afterSaleOrderMapper.updateById(any(AfterSaleOrder.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            afterSaleService.fillLogistics(request, 1L));
    }

    @Test
    @DisplayName("确认收货 - 成功")
    void confirmReceive_Success() {
        // Given
        AfterSaleOrder order = createTestAfterSaleOrder();
        order.setStatus(AfterSaleStatus.RETURNED.getCode());

        when(afterSaleOrderMapper.selectById(1L)).thenReturn(order);
        when(afterSaleOrderMapper.updateById(any(AfterSaleOrder.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            afterSaleService.confirmReceive(1L));
    }

    @Test
    @DisplayName("取消售后 - 成功")
    void cancelAfterSale_Success() {
        // Given
        AfterSaleOrder order = createTestAfterSaleOrder();
        order.setStatus(AfterSaleStatus.PENDING.getCode());

        when(afterSaleOrderMapper.selectById(1L)).thenReturn(order);
        when(afterSaleOrderMapper.updateById(any(AfterSaleOrder.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            afterSaleService.cancelAfterSale(1L, 1L, "不想要了"));
    }

    @Test
    @DisplayName("获取售后统计 - 成功")
    void getStatistics_Success() {
        // Given
        when(afterSaleOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(afterSaleOrderMapper.countByStatus()).thenReturn(new ArrayList<>());

        // When
        var stats = afterSaleService.getStatistics(1L);

        // Then
        assertNotNull(stats);
    }

    // Helper methods

    private AfterSaleOrder createTestAfterSaleOrder() {
        AfterSaleOrder order = new AfterSaleOrder();
        order.setId(1L);
        order.setAfterSaleNo("AS202603240001");
        order.setOrderId(1L);
        order.setOrderNo("ORD202603240001");
        order.setShopId(1L);
        order.setUserId(1L);
        order.setType(AfterSaleType.REFUND_ONLY.getCode());
        order.setStatus(AfterSaleStatus.PENDING.getCode());
        order.setRefundAmount(new BigDecimal("100.00"));
        order.setReasonId(1L);
        order.setReason("不想要了");
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}