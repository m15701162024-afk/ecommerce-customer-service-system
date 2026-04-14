package com.ecommerce.workorder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.workorder.dto.request.*;
import com.ecommerce.workorder.dto.response.WorkOrderStatsResponse;
import com.ecommerce.workorder.entity.WorkOrder;
import com.ecommerce.workorder.entity.WorkOrderCategory;
import com.ecommerce.workorder.entity.WorkOrderFlow;
import com.ecommerce.workorder.mapper.WorkOrderCategoryMapper;
import com.ecommerce.workorder.mapper.WorkOrderFlowMapper;
import com.ecommerce.workorder.mapper.WorkOrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 工单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderMapper workOrderMapper;

    @Mock
    private WorkOrderCategoryMapper categoryMapper;

    @Mock
    private WorkOrderFlowMapper flowMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WorkOrderService workOrderService;

    private WorkOrderCategory testCategory;
    private CreateWorkOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        testCategory = new WorkOrderCategory();
        testCategory.setId(1L);
        testCategory.setName("咨询类");
        testCategory.setCode("CONSULT");
        testCategory.setEnabled(true);
        testCategory.setDefaultPriority("NORMAL");
        testCategory.setDefaultResponseTime(4);
        testCategory.setDefaultResolveTime(24);

        createRequest = new CreateWorkOrderRequest();
        createRequest.setCategoryId(1L);
        createRequest.setTitle("测试工单");
        createRequest.setDescription("这是一个测试工单");
        createRequest.setBuyerId("buyer-001");
        createRequest.setShopId(1L);
        createRequest.setPlatform("DOUYIN");
    }

    @Test
    @DisplayName("创建工单 - 成功")
    void createWorkOrder_Success() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);
        when(workOrderMapper.insert(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });
        when(flowMapper.insert(any(WorkOrderFlow.class))).thenReturn(1);

        // When
        Long result = workOrderService.createWorkOrder(createRequest, 1L, "admin");

        // Then
        assertNotNull(result);
        verify(workOrderMapper, times(1)).insert(any(WorkOrder.class));
        verify(flowMapper, times(1)).insert(any(WorkOrderFlow.class));
    }

    @Test
    @DisplayName("创建工单 - 分类不存在")
    void createWorkOrder_CategoryNotFound() {
        // Given
        when(categoryMapper.selectById(1L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            workOrderService.createWorkOrder(createRequest, 1L, "admin"));
    }

    @Test
    @DisplayName("创建工单 - 分类已禁用")
    void createWorkOrder_CategoryDisabled() {
        // Given
        testCategory.setEnabled(false);
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            workOrderService.createWorkOrder(createRequest, 1L, "admin"));
    }

    @Test
    @DisplayName("分配工单 - 成功")
    void assignWorkOrder_Success() {
        // Given
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.setStatus("PENDING");
        
        AssignWorkOrderRequest request = new AssignWorkOrderRequest();
        request.setWorkOrderId(1L);
        request.setAssigneeId(2L);
        request.setAssigneeName("客服小王");

        when(workOrderMapper.selectById(1L)).thenReturn(workOrder);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        when(flowMapper.insert(any(WorkOrderFlow.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            workOrderService.assignWorkOrder(request, 1L, "admin"));
        
        verify(workOrderMapper, times(1)).updateById(any(WorkOrder.class));
    }

    @Test
    @DisplayName("分配工单 - 状态不允许")
    void assignWorkOrder_InvalidStatus() {
        // Given
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.setStatus("PROCESSING");
        
        AssignWorkOrderRequest request = new AssignWorkOrderRequest();
        request.setWorkOrderId(1L);

        when(workOrderMapper.selectById(1L)).thenReturn(workOrder);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            workOrderService.assignWorkOrder(request, 1L, "admin"));
    }

    @Test
    @DisplayName("解决工单 - 成功")
    void resolveWorkOrder_Success() {
        // Given
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.setStatus("PROCESSING");
        
        ResolveWorkOrderRequest request = new ResolveWorkOrderRequest();
        request.setWorkOrderId(1L);
        request.setSolution("问题已解决");

        when(workOrderMapper.selectById(1L)).thenReturn(workOrder);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        when(flowMapper.insert(any(WorkOrderFlow.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            workOrderService.resolveWorkOrder(request, 1L, "admin"));
    }

    @Test
    @DisplayName("关闭工单 - 成功")
    void closeWorkOrder_Success() {
        // Given
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.setStatus("RESOLVED");
        
        CloseWorkOrderRequest request = new CloseWorkOrderRequest();
        request.setWorkOrderId(1L);
        request.setSatisfactionScore(5);

        when(workOrderMapper.selectById(1L)).thenReturn(workOrder);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        when(flowMapper.insert(any(WorkOrderFlow.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            workOrderService.closeWorkOrder(request, 1L, "admin"));
    }

    @Test
    @DisplayName("重开工单 - 成功")
    void reopenWorkOrder_Success() {
        // Given
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.setStatus("CLOSED");

        when(workOrderMapper.selectById(1L)).thenReturn(workOrder);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);
        when(flowMapper.insert(any(WorkOrderFlow.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            workOrderService.reopenWorkOrder(1L, "需要重新处理", 1L, "admin"));
    }

    @Test
    @DisplayName("获取统计 - 成功")
    void getStats_Success() {
        // Given
        when(workOrderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(workOrderMapper.countByStatus()).thenReturn(new ArrayList<>());
        when(workOrderMapper.countByPriority()).thenReturn(new ArrayList<>());
        when(workOrderMapper.countByCategory()).thenReturn(new ArrayList<>());
        when(workOrderMapper.countByAssignee()).thenReturn(new ArrayList<>());

        // When
        WorkOrderStatsResponse stats = workOrderService.getStats(null, null, null);

        // Then
        assertNotNull(stats);
        assertEquals(10L, stats.getTotalCount());
    }

    @Test
    @DisplayName("获取超时工单 - 成功")
    void getOverdueOrders_Success() {
        // Given
        List<WorkOrder> overdueOrders = new ArrayList<>();
        overdueOrders.add(createTestWorkOrder());
        
        when(workOrderMapper.findOverdueOrders(any(LocalDateTime.class))).thenReturn(overdueOrders);

        // When
        List<WorkOrder> result = workOrderService.getOverdueOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Helper methods

    private WorkOrder createTestWorkOrder() {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(1L);
        workOrder.setOrderNo("WO202603240001");
        workOrder.setTitle("测试工单");
        workOrder.setDescription("测试描述");
        workOrder.setStatus("PENDING");
        workOrder.setPriority("NORMAL");
        workOrder.setCategoryId(1L);
        workOrder.setBuyerId("buyer-001");
        workOrder.setShopId(1L);
        workOrder.setPlatform("DOUYIN");
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setSlaResponseTime(LocalDateTime.now().plusHours(4));
        workOrder.setSlaResolveTime(LocalDateTime.now().plusHours(24));
        return workOrder;
    }
}