package com.ecommerce.purchase.service;

import com.ecommerce.purchase.entity.PurchaseOrder;
import com.ecommerce.purchase.entity.PurchaseOrder.OrderStatus;
import com.ecommerce.purchase.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseOrderRepository repository;

    @InjectMocks
    private PurchaseService service;

    private PurchaseOrder testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new PurchaseOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo("PO202401010001");
        testOrder.setSupplierName("Test Supplier");
        testOrder.setProductName("Test Product");
        testOrder.setQuantity(10);
        testOrder.setUnitPrice(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("创建订单 - 金额小于1000自动支付")
    void createOrder_AutoPay_WhenAmountLessThan1000() {
        testOrder.setUnitPrice(new BigDecimal("50.00"));
        testOrder.setQuantity(10);

        when(repository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PurchaseOrder result = service.createOrder(testOrder);

        assertEquals(OrderStatus.AUTO_PAID, result.getStatus());
        assertNotNull(result.getPayTime());
        assertEquals(new BigDecimal("500.00"), result.getTotalAmount());
        verify(repository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("创建订单 - 金额1000-10000需人工确认")
    void createOrder_NeedsConfirm_WhenAmountBetween1000And10000() {
        testOrder.setUnitPrice(new BigDecimal("100.00"));
        testOrder.setQuantity(15);

        when(repository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PurchaseOrder result = service.createOrder(testOrder);

        assertEquals(OrderStatus.CONFIRMING, result.getStatus());
        assertNull(result.getPayTime());
        assertEquals(new BigDecimal("1500.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("创建订单 - 金额超过10000预警")
    void createOrder_Warning_WhenAmountGreaterThan10000() {
        testOrder.setUnitPrice(new BigDecimal("200.00"));
        testOrder.setQuantity(60);

        when(repository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PurchaseOrder result = service.createOrder(testOrder);

        assertEquals(OrderStatus.WARNING, result.getStatus());
        assertEquals(new BigDecimal("12000.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("确认订单 - 成功")
    void confirmOrder_Success() {
        testOrder.setStatus(OrderStatus.CONFIRMING);
        testOrder.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PurchaseOrder.class))).thenReturn(testOrder);

        PurchaseOrder result = service.confirmOrder(1L);

        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getPayTime());
    }

    @Test
    @DisplayName("确认订单 - 状态不允许时报错")
    void confirmOrder_ThrowsException_WhenStatusNotAllowed() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        testOrder.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () -> service.confirmOrder(1L));
    }

    @Test
    @DisplayName("拒绝订单 - 成功")
    void rejectOrder_Success() {
        testOrder.setStatus(OrderStatus.CONFIRMING);
        testOrder.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PurchaseOrder.class))).thenReturn(testOrder);

        PurchaseOrder result = service.rejectOrder(1L, "测试拒绝原因");

        assertEquals(OrderStatus.REJECTED, result.getStatus());
        assertEquals("测试拒绝原因", result.getRemark());
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_Success() {
        testOrder.setStatus(OrderStatus.CONFIRMING);
        testOrder.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repository.save(any(PurchaseOrder.class))).thenReturn(testOrder);

        PurchaseOrder result = service.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("取消订单 - 已完成订单不能取消")
    void cancelOrder_ThrowsException_WhenOrderCompleted() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        testOrder.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () -> service.cancelOrder(1L));
    }

    @Test
    @DisplayName("获取所有订单")
    void getAllOrders_ReturnsAllOrders() {
        List<PurchaseOrder> orders = Arrays.asList(testOrder, new PurchaseOrder());
        when(repository.findAllByOrderByCreateTimeDesc()).thenReturn(orders);

        List<PurchaseOrder> result = service.getAllOrders();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("按状态获取订单")
    void getOrdersByStatus_ReturnsFilteredOrders() {
        testOrder.setStatus(OrderStatus.CONFIRMING);
        List<PurchaseOrder> orders = List.of(testOrder);
        when(repository.findByStatus(OrderStatus.CONFIRMING)).thenReturn(orders);

        List<PurchaseOrder> result = service.getOrdersByStatus(OrderStatus.CONFIRMING);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.CONFIRMING, result.get(0).getStatus());
    }

    @Test
    @DisplayName("获取统计数据")
    void getStatistics_ReturnsCorrectStats() {
        PurchaseOrder order1 = new PurchaseOrder();
        order1.setTotalAmount(new BigDecimal("500.00"));
        order1.setStatus(OrderStatus.AUTO_PAID);

        PurchaseOrder order2 = new PurchaseOrder();
        order2.setTotalAmount(new BigDecimal("2000.00"));
        order2.setStatus(OrderStatus.CONFIRMING);

        when(repository.findAll()).thenReturn(Arrays.asList(order1, order2));

        var stats = service.getStatistics();

        assertEquals(2, stats.get("totalOrders"));
        assertNotNull(stats.get("totalAmount"));
        assertNotNull(stats.get("paidAmount"));
        assertNotNull(stats.get("pendingAmount"));
    }
}