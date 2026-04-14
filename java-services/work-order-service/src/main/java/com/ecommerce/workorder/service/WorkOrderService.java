package com.ecommerce.workorder.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.PageResult;
import com.ecommerce.workorder.dto.request.*;
import com.ecommerce.workorder.dto.response.*;
import com.ecommerce.workorder.entity.WorkOrder;
import com.ecommerce.workorder.entity.WorkOrderCategory;
import com.ecommerce.workorder.entity.WorkOrderFlow;
import com.ecommerce.workorder.mapper.WorkOrderCategoryMapper;
import com.ecommerce.workorder.mapper.WorkOrderFlowMapper;
import com.ecommerce.workorder.mapper.WorkOrderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ecommerce.workorder.service.WorkdayService;

/**
 * 工单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderCategoryMapper categoryMapper;
    private final WorkOrderFlowMapper flowMapper;
    private final ObjectMapper objectMapper;
    private final WorkdayService workdayService;

    // 工单状态
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_REOPENED = "REOPENED";

    // 优先级
    public static final String PRIORITY_URGENT = "URGENT";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_LOW = "LOW";

    // SLA时效配置(小时)
    private static final Map<String, Integer> SLA_RESPONSE_HOURS = Map.of(
        PRIORITY_URGENT, 1,
        PRIORITY_HIGH, 2,
        PRIORITY_NORMAL, 4,
        PRIORITY_LOW, 8
    );

    private static final Map<String, Integer> SLA_RESOLVE_HOURS = Map.of(
        PRIORITY_URGENT, 4,
        PRIORITY_HIGH, 8,
        PRIORITY_NORMAL, 24,
        PRIORITY_LOW, 48
    );

    /**
     * 创建工单
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkOrder(CreateWorkOrderRequest request, Long operatorId, String operatorName) {
        // 查询分类
        WorkOrderCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || !Boolean.TRUE.equals(category.getEnabled())) {
            throw new BusinessException("工单分类不存在或已禁用");
        }

        // 创建工单
        WorkOrder workOrder = new WorkOrder();
        BeanUtils.copyProperties(request, workOrder);
        
        // 生成工单编号
        workOrder.setOrderNo(generateOrderNo());
        
        // 设置状态
        workOrder.setStatus(STATUS_PENDING);
        
        // 设置优先级(使用分类默认值或请求值)
        String priority = StringUtils.hasText(request.getPriority()) ? 
            request.getPriority() : category.getDefaultPriority();
        if (!StringUtils.hasText(priority)) {
            priority = PRIORITY_NORMAL;
        }
        workOrder.setPriority(priority);
        
        // 计算SLA时效（使用工作日历）
        LocalDateTime now = LocalDateTime.now();
        Integer responseHours = category.getDefaultResponseTime();
        Integer resolveHours = category.getDefaultResolveTime();
        
        if (responseHours == null) {
            responseHours = SLA_RESPONSE_HOURS.getOrDefault(priority, 4);
        }
        if (resolveHours == null) {
            resolveHours = SLA_RESOLVE_HOURS.getOrDefault(priority, 24);
        }
        
        workOrder.setSlaResponseTime(workdayService.addWorkHours(now, responseHours));
        workOrder.setSlaResolveTime(workdayService.addWorkHours(now, resolveHours));
        
        // 处理标签和附件
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                workOrder.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                log.error("序列化标签失败", e);
            }
        }
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            try {
                workOrder.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
            } catch (JsonProcessingException e) {
                log.error("序列化附件失败", e);
            }
        }
        
        workOrderMapper.insert(workOrder);
        
        // 记录流转
        createFlow(workOrder.getId(), "CREATE", null, STATUS_PENDING, 
            null, null, null, null, operatorId, operatorName, "创建工单", request.getSource());
        
        log.info("工单创建成功, id={}, orderNo={}", workOrder.getId(), workOrder.getOrderNo());
        return workOrder.getId();
    }

    /**
     * 分页查询工单列表
     */
    public PageResult<WorkOrderResponse> listWorkOrders(QueryWorkOrderRequest request) {
        Page<WorkOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (StringUtils.hasText(request.getOrderNo())) {
            wrapper.eq(WorkOrder::getOrderNo, request.getOrderNo());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(WorkOrder::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getPriority())) {
            wrapper.eq(WorkOrder::getPriority, request.getPriority());
        }
        if (request.getCategoryId() != null) {
            wrapper.eq(WorkOrder::getCategoryId, request.getCategoryId());
        }
        if (request.getAssigneeId() != null) {
            wrapper.eq(WorkOrder::getAssigneeId, request.getAssigneeId());
        }
        if (request.getBuyerId() != null) {
            wrapper.eq(WorkOrder::getBuyerId, request.getBuyerId());
        }
        if (request.getShopId() != null) {
            wrapper.eq(WorkOrder::getShopId, request.getShopId());
        }
        if (StringUtils.hasText(request.getPlatform())) {
            wrapper.eq(WorkOrder::getPlatform, request.getPlatform());
        }
        if (request.getTeamId() != null) {
            wrapper.eq(WorkOrder::getTeamId, request.getTeamId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(WorkOrder::getTitle, request.getKeyword())
                .or().like(WorkOrder::getDescription, request.getKeyword()));
        }
        if (request.getCreatedAtStart() != null) {
            wrapper.ge(WorkOrder::getCreatedAt, request.getCreatedAtStart());
        }
        if (request.getCreatedAtEnd() != null) {
            wrapper.le(WorkOrder::getCreatedAt, request.getCreatedAtEnd());
        }
        
        // 超时查询
        if (Boolean.TRUE.equals(request.getOverdue())) {
            LocalDateTime now = LocalDateTime.now();
            wrapper.and(w -> w.lt(WorkOrder::getSlaResponseTime, now)
                .or().lt(WorkOrder::getSlaResolveTime, now));
            wrapper.in(WorkOrder::getStatus, STATUS_PENDING, STATUS_ASSIGNED, STATUS_PROCESSING);
        }
        
        // 已响应查询
        if (Boolean.TRUE.equals(request.getResponded())) {
            wrapper.isNotNull(WorkOrder::getFirstResponseTime);
        }
        
        wrapper.orderByDesc(WorkOrder::getCreatedAt);
        
        Page<WorkOrder> result = workOrderMapper.selectPage(page, wrapper);
        
        // 转换响应
        List<WorkOrderResponse> responses = result.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        return PageResult.of(responses, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取工单详情
     */
    public WorkOrderDetailResponse getWorkOrderDetail(Long id) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        
        WorkOrderDetailResponse response = new WorkOrderDetailResponse();
        BeanUtils.copyProperties(workOrder, response);
        
        // 设置分类名称
        WorkOrderCategory category = categoryMapper.selectById(workOrder.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
        }
        
        // 设置是否超时
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = (workOrder.getSlaResponseTime() != null && workOrder.getSlaResponseTime().isBefore(now))
            || (workOrder.getSlaResolveTime() != null && workOrder.getSlaResolveTime().isBefore(now));
        response.setOverdue(isOverdue && !isFinalStatus(workOrder.getStatus()));
        
        // 解析标签和附件
        if (StringUtils.hasText(workOrder.getTags())) {
            try {
                response.setTags(objectMapper.readValue(workOrder.getTags(), new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.error("解析标签失败", e);
            }
        }
        if (StringUtils.hasText(workOrder.getAttachments())) {
            try {
                response.setAttachments(objectMapper.readValue(workOrder.getAttachments(), new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.error("解析附件失败", e);
            }
        }
        
        // 查询流转记录
        LambdaQueryWrapper<WorkOrderFlow> flowWrapper = new LambdaQueryWrapper<>();
        flowWrapper.eq(WorkOrderFlow::getWorkOrderId, id)
            .orderByDesc(WorkOrderFlow::getCreatedAt);
        List<WorkOrderFlow> flows = flowMapper.selectList(flowWrapper);
        
        List<WorkOrderFlowResponse> flowResponses = flows.stream()
            .map(this::convertToFlowResponse)
            .collect(Collectors.toList());
        response.setFlows(flowResponses);
        
        return response;
    }

    /**
     * 分配工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignWorkOrder(AssignWorkOrderRequest request, Long operatorId, String operatorName) {
        WorkOrder workOrder = getAndValidateWorkOrder(request.getWorkOrderId());
        
        if (!STATUS_PENDING.equals(workOrder.getStatus()) && !STATUS_REOPENED.equals(workOrder.getStatus())) {
            throw new BusinessException("当前状态不允许分配");
        }
        
        String oldStatus = workOrder.getStatus();
        Long oldAssigneeId = workOrder.getAssigneeId();
        String oldAssigneeName = workOrder.getAssigneeName();
        
        workOrder.setStatus(STATUS_ASSIGNED);
        workOrder.setAssigneeId(request.getAssigneeId());
        workOrder.setAssigneeName(request.getAssigneeName());
        if (request.getTeamId() != null) {
            workOrder.setTeamId(request.getTeamId());
        }
        
        workOrderMapper.updateById(workOrder);
        
        // 记录流转
        createFlow(workOrder.getId(), "ASSIGN", oldStatus, STATUS_ASSIGNED,
            oldAssigneeId, oldAssigneeName, request.getAssigneeId(), request.getAssigneeName(),
            operatorId, operatorName, request.getRemark(), "MANUAL");
        
        log.info("工单分配成功, orderId={}, assigneeId={}", workOrder.getId(), request.getAssigneeId());
    }

    /**
     * 转派工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void transferWorkOrder(TransferWorkOrderRequest request, Long operatorId, String operatorName) {
        WorkOrder workOrder = getAndValidateWorkOrder(request.getWorkOrderId());
        
        if (STATUS_PENDING.equals(workOrder.getStatus()) || STATUS_CLOSED.equals(workOrder.getStatus())) {
            throw new BusinessException("当前状态不允许转派");
        }
        
        Long oldAssigneeId = workOrder.getAssigneeId();
        String oldAssigneeName = workOrder.getAssigneeName();
        
        workOrder.setAssigneeId(request.getNewAssigneeId());
        workOrder.setAssigneeName(request.getNewAssigneeName());
        if (request.getNewTeamId() != null) {
            workOrder.setTeamId(request.getNewTeamId());
        }
        
        workOrderMapper.updateById(workOrder);
        
        // 记录流转
        createFlow(workOrder.getId(), "TRANSFER", workOrder.getStatus(), workOrder.getStatus(),
            oldAssigneeId, oldAssigneeName, request.getNewAssigneeId(), request.getNewAssigneeName(),
            operatorId, operatorName, request.getReason(), "MANUAL");
        
        log.info("工单转派成功, orderId={}, newAssigneeId={}", workOrder.getId(), request.getNewAssigneeId());
    }

    /**
     * 开始处理工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void startProcessing(Long workOrderId, Long operatorId, String operatorName) {
        WorkOrder workOrder = getAndValidateWorkOrder(workOrderId);
        
        if (!STATUS_ASSIGNED.equals(workOrder.getStatus())) {
            throw new BusinessException("只有已分配状态的工单才能开始处理");
        }
        
        String oldStatus = workOrder.getStatus();
        workOrder.setStatus(STATUS_PROCESSING);
        
        // 设置首次响应时间
        if (workOrder.getFirstResponseTime() == null) {
            workOrder.setFirstResponseTime(LocalDateTime.now());
        }
        
        workOrderMapper.updateById(workOrder);
        
        createFlow(workOrder.getId(), "PROCESS", oldStatus, STATUS_PROCESSING,
            workOrder.getAssigneeId(), workOrder.getAssigneeName(),
            workOrder.getAssigneeId(), workOrder.getAssigneeName(),
            operatorId, operatorName, "开始处理", "MANUAL");
    }

    /**
     * 解决工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void resolveWorkOrder(ResolveWorkOrderRequest request, Long operatorId, String operatorName) {
        WorkOrder workOrder = getAndValidateWorkOrder(request.getWorkOrderId());
        
        if (!STATUS_PROCESSING.equals(workOrder.getStatus())) {
            throw new BusinessException("只有处理中状态的工单才能解决");
        }
        
        String oldStatus = workOrder.getStatus();
        LocalDateTime now = LocalDateTime.now();
        
        workOrder.setStatus(STATUS_RESOLVED);
        workOrder.setSolution(request.getSolution());
        workOrder.setResolvedAt(now);
        
        // 设置首次响应时间
        if (workOrder.getFirstResponseTime() == null) {
            workOrder.setFirstResponseTime(now);
        }
        
        workOrderMapper.updateById(workOrder);
        
        createFlow(workOrder.getId(), "RESOLVE", oldStatus, STATUS_RESOLVED,
            workOrder.getAssigneeId(), workOrder.getAssigneeName(),
            workOrder.getAssigneeId(), workOrder.getAssigneeName(),
            operatorId, operatorName, request.getRemark(), "MANUAL");
        
        log.info("工单解决成功, orderId={}", workOrder.getId());
    }

    /**
     * 关闭工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void closeWorkOrder(CloseWorkOrderRequest request, Long operatorId, String operatorName) {
        WorkOrder workOrder = getAndValidateWorkOrder(request.getWorkOrderId());
        
        if (!STATUS_RESOLVED.equals(workOrder.getStatus())) {
            throw new BusinessException("只有已解决状态的工单才能关闭");
        }
        
        String oldStatus = workOrder.getStatus();
        
        workOrder.setStatus(STATUS_CLOSED);
        workOrder.setClosedAt(LocalDateTime.now());
        if (request.getSatisfactionScore() != null) {
            workOrder.setSatisfactionScore(request.getSatisfactionScore());
        }
        if (StringUtils.hasText(request.getFeedback())) {
            workOrder.setFeedback(request.getFeedback());
        }
        
        workOrderMapper.updateById(workOrder);
        
        createFlow(workOrder.getId(), "CLOSE", oldStatus, STATUS_CLOSED,
            workOrder.getAssigneeId(), workOrder.getAssigneeName(),
            null, null, operatorId, operatorName, request.getReason(), "MANUAL");
        
        log.info("工单关闭成功, orderId={}", workOrder.getId());
    }

    /**
     * 重开工单
     */
    @Transactional(rollbackFor = Exception.class)
    public void reopenWorkOrder(Long workOrderId, String reason, Long operatorId, String operatorName) {
        WorkOrder workOrder = getAndValidateWorkOrder(workOrderId);
        
        if (!STATUS_CLOSED.equals(workOrder.getStatus())) {
            throw new BusinessException("只有已关闭状态的工单才能重开");
        }
        
        String oldStatus = workOrder.getStatus();
        LocalDateTime now = LocalDateTime.now();
        
        workOrder.setStatus(STATUS_REOPENED);
        workOrder.setAssigneeId(null);
        workOrder.setAssigneeName(null);
        
        // 重新计算SLA（使用工作日历）
        Integer resolveHours = SLA_RESOLVE_HOURS.getOrDefault(workOrder.getPriority(), 24);
        workOrder.setSlaResponseTime(workdayService.addWorkHours(now, SLA_RESPONSE_HOURS.getOrDefault(workOrder.getPriority(), 4)));
        workOrder.setSlaResolveTime(workdayService.addWorkHours(now, resolveHours));
        
        workOrderMapper.updateById(workOrder);
        
        createFlow(workOrder.getId(), "REOPEN", oldStatus, STATUS_REOPENED,
            workOrder.getAssigneeId(), workOrder.getAssigneeName(),
            null, null, operatorId, operatorName, reason, "MANUAL");
        
        log.info("工单重开成功, orderId={}", workOrder.getId());
    }

    /**
     * 获取工单统计
     */
    public WorkOrderStatsResponse getStats(Long shopId, Long teamId, Long assigneeId) {
        WorkOrderStatsResponse stats = new WorkOrderStatsResponse();
        
        // 基础统计
        LambdaQueryWrapper<WorkOrder> baseWrapper = new LambdaQueryWrapper<>();
        if (shopId != null) {
            baseWrapper.eq(WorkOrder::getShopId, shopId);
        }
        if (teamId != null) {
            baseWrapper.eq(WorkOrder::getTeamId, teamId);
        }
        if (assigneeId != null) {
            baseWrapper.eq(WorkOrder::getAssigneeId, assigneeId);
        }
        
        Long totalCount = workOrderMapper.selectCount(baseWrapper);
        stats.setTotalCount(totalCount);
        
        // 按状态统计
        LambdaQueryWrapper<WorkOrder> pendingWrapper = baseWrapper.clone();
        pendingWrapper.eq(WorkOrder::getStatus, STATUS_PENDING);
        stats.setPendingCount(workOrderMapper.selectCount(pendingWrapper));
        
        LambdaQueryWrapper<WorkOrder> assignedWrapper = baseWrapper.clone();
        assignedWrapper.eq(WorkOrder::getStatus, STATUS_ASSIGNED);
        Long assignedCount = workOrderMapper.selectCount(assignedWrapper);
        
        LambdaQueryWrapper<WorkOrder> processingWrapper = baseWrapper.clone();
        processingWrapper.eq(WorkOrder::getStatus, STATUS_PROCESSING);
        Long processingCount = workOrderMapper.selectCount(processingWrapper);
        stats.setProcessingCount(assignedCount + processingCount);
        
        LambdaQueryWrapper<WorkOrder> resolvedWrapper = baseWrapper.clone();
        resolvedWrapper.eq(WorkOrder::getStatus, STATUS_RESOLVED);
        stats.setResolvedCount(workOrderMapper.selectCount(resolvedWrapper));
        
        LambdaQueryWrapper<WorkOrder> closedWrapper = baseWrapper.clone();
        closedWrapper.eq(WorkOrder::getStatus, STATUS_CLOSED);
        stats.setClosedCount(workOrderMapper.selectCount(closedWrapper));
        
        // 超时统计
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<WorkOrder> overdueWrapper = baseWrapper.clone();
        overdueWrapper.in(WorkOrder::getStatus, STATUS_PENDING, STATUS_ASSIGNED, STATUS_PROCESSING)
            .and(w -> w.lt(WorkOrder::getSlaResponseTime, now)
                .or().lt(WorkOrder::getSlaResolveTime, now));
        stats.setOverdueCount(workOrderMapper.selectCount(overdueWrapper));
        
        // 今日统计
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LambdaQueryWrapper<WorkOrder> todayCreatedWrapper = baseWrapper.clone();
        todayCreatedWrapper.ge(WorkOrder::getCreatedAt, todayStart);
        stats.setTodayCreatedCount(workOrderMapper.selectCount(todayCreatedWrapper));
        
        LambdaQueryWrapper<WorkOrder> todayResolvedWrapper = baseWrapper.clone();
        todayResolvedWrapper.ge(WorkOrder::getResolvedAt, todayStart);
        stats.setTodayResolvedCount(workOrderMapper.selectCount(todayResolvedWrapper));
        
        // 按状态/优先级/分类统计
        stats.setStatusStats(workOrderMapper.countByStatus());
        stats.setPriorityStats(workOrderMapper.countByPriority());
        stats.setCategoryStats(workOrderMapper.countByCategory());
        stats.setAssigneeStats(workOrderMapper.countByAssignee());
        
        return stats;
    }

    /**
     * 获取超时工单列表
     */
    public List<WorkOrder> getOverdueOrders() {
        return workOrderMapper.findOverdueOrders(LocalDateTime.now());
    }

    /**
     * 获取即将超时工单列表
     */
    public List<WorkOrder> getNearDueOrders(int hoursBefore) {
        LocalDateTime now = LocalDateTime.now();
        return workOrderMapper.findNearDueOrders(now, now.plusHours(hoursBefore));
    }

    // ========== 私有方法 ==========

    private WorkOrder getAndValidateWorkOrder(Long id) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException("工单不存在");
        }
        return workOrder;
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
        return "WO" + dateStr + randomStr;
    }

    private void createFlow(Long workOrderId, String action, String fromStatus, String toStatus,
                           Long fromAssigneeId, String fromAssigneeName,
                           Long toAssigneeId, String toAssigneeName,
                           Long operatorId, String operatorName, String remark, String source) {
        WorkOrderFlow flow = new WorkOrderFlow();
        flow.setWorkOrderId(workOrderId);
        flow.setAction(action);
        flow.setFromStatus(fromStatus);
        flow.setToStatus(toStatus);
        flow.setFromAssigneeId(fromAssigneeId);
        flow.setFromAssigneeName(fromAssigneeName);
        flow.setToAssigneeId(toAssigneeId);
        flow.setToAssigneeName(toAssigneeName);
        flow.setOperatorId(operatorId);
        flow.setOperatorName(operatorName);
        flow.setRemark(remark);
        flow.setSource(source);
        flowMapper.insert(flow);
    }

    private WorkOrderResponse convertToResponse(WorkOrder workOrder) {
        WorkOrderResponse response = new WorkOrderResponse();
        BeanUtils.copyProperties(workOrder, response);
        
        // 设置分类名称
        WorkOrderCategory category = categoryMapper.selectById(workOrder.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
        }
        
        // 设置是否超时
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = (workOrder.getSlaResponseTime() != null && workOrder.getSlaResponseTime().isBefore(now))
            || (workOrder.getSlaResolveTime() != null && workOrder.getSlaResolveTime().isBefore(now));
        response.setOverdue(isOverdue && !isFinalStatus(workOrder.getStatus()));
        
        return response;
    }

    private WorkOrderFlowResponse convertToFlowResponse(WorkOrderFlow flow) {
        WorkOrderFlowResponse response = new WorkOrderFlowResponse();
        BeanUtils.copyProperties(flow, response);
        return response;
    }

    private boolean isFinalStatus(String status) {
        return STATUS_RESOLVED.equals(status) || STATUS_CLOSED.equals(status);
    }
}