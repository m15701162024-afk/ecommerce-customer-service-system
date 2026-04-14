package com.ecommerce.aftersale.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.aftersale.client.OrderServiceClient;
import com.ecommerce.aftersale.dto.OrderDTO;
import com.ecommerce.aftersale.dto.request.*;
import com.ecommerce.aftersale.dto.response.*;
import com.ecommerce.aftersale.entity.AfterSaleOrder;
import com.ecommerce.aftersale.entity.AfterSaleReason;
import com.ecommerce.aftersale.entity.ReturnAddress;
import com.ecommerce.aftersale.entity.PaymentRecord;
import com.ecommerce.aftersale.enums.AfterSaleStatus;
import com.ecommerce.aftersale.enums.AfterSaleType;
import com.ecommerce.aftersale.mapper.AfterSaleOrderMapper;
import com.ecommerce.aftersale.mapper.AfterSaleReasonMapper;
import com.ecommerce.aftersale.mapper.ReturnAddressMapper;
import com.ecommerce.common.audit.AuditLog;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.lock.DistributedLock;
import com.ecommerce.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 售后服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AfterSaleService {

    private final AfterSaleOrderMapper afterSaleOrderMapper;
    private final AfterSaleReasonMapper afterSaleReasonMapper;
    private final ReturnAddressMapper returnAddressMapper;
    private final PaymentService paymentService;
    private final OrderServiceClient orderServiceClient;
    private final DistributedLock distributedLock;

    /**
     * 申请售后
     */
    @Transactional(rollbackFor = Exception.class)
    public Long applyAfterSale(ApplyAfterSaleRequest request, Long userId, Long shopId, String platform) {
        String lockKey = "after_sale:order:" + request.getOrderId();
        
        boolean locked = distributedLock.tryLock(lockKey, 3000, 10000);
        if (!locked) {
            throw new BusinessException("订单正在处理中，请勿重复提交");
        }
        
        try {
            return applyAfterSaleInternal(request, userId, shopId, platform);
        } finally {
            distributedLock.unlock(lockKey);
        }
    }
    
    private Long applyAfterSaleInternal(ApplyAfterSaleRequest request, Long userId, Long shopId, String platform) {
        AfterSaleType type = AfterSaleType.fromCode(request.getType());

        // 验证退款金额
        if ((type == AfterSaleType.REFUND_ONLY || type == AfterSaleType.RETURN_REFUND) 
                && request.getRefundAmount() == null) {
            throw new BusinessException("退款金额不能为空");
        }

        // AS-001: 验证退款金额不超过订单实付金额
        if ((type == AfterSaleType.REFUND_ONLY || type == AfterSaleType.RETURN_REFUND) 
                && request.getRefundAmount() != null) {
            validateRefundAmount(request.getRefundAmount(), request.getOrderId());
        }

        // 验证换货信息
        if (type == AfterSaleType.EXCHANGE) {
            if (!StringUtils.hasText(request.getExchangeSku())) {
                throw new BusinessException("换货SKU不能为空");
            }
            if (request.getExchangeQuantity() == null || request.getExchangeQuantity() <= 0) {
                throw new BusinessException("换货数量必须大于0");
            }
        }

        // 验证售后原因
        AfterSaleReason reason = afterSaleReasonMapper.selectById(request.getReasonId());
        if (reason == null || !reason.getEnabled()) {
            throw new BusinessException("售后原因不存在或已禁用");
        }

        // 检查是否已存在进行中的售后单
        LambdaQueryWrapper<AfterSaleOrder> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(AfterSaleOrder::getOrderId, request.getOrderId());
        existWrapper.in(AfterSaleOrder::getStatus, 
                AfterSaleStatus.PENDING.getCode(),
                AfterSaleStatus.APPROVED.getCode(),
                AfterSaleStatus.RETURNING.getCode(),
                AfterSaleStatus.RETURNED.getCode(),
                AfterSaleStatus.REFUNDING.getCode(),
                AfterSaleStatus.EXCHANGING.getCode());
        if (afterSaleOrderMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException("该订单已存在进行中的售后申请");
        }

        // 创建售后单
        AfterSaleOrder order = new AfterSaleOrder();
        BeanUtils.copyProperties(request, order);
        order.setAfterSaleNo(generateAfterSaleNo());
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setPlatform(platform);
        order.setStatus(AfterSaleStatus.PENDING.getCode());
        order.setDeleted(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        afterSaleOrderMapper.insert(order);

        log.info("售后申请创建成功, afterSaleNo={}, orderId={}, type={}", 
                order.getAfterSaleNo(), order.getOrderId(), order.getType());

        return order.getId();
    }

    /**
     * 生成售后单号
     */
    private String generateAfterSaleNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "AS" + dateStr + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }

    /**
     * 分页查询售后列表
     */
    public PageResult<AfterSaleListResponse> listAfterSales(AfterSaleQueryRequest request) {
        Page<AfterSaleOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<AfterSaleOrder> wrapper = new LambdaQueryWrapper<>();

        // 条件过滤
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(AfterSaleOrder::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getType())) {
            wrapper.eq(AfterSaleOrder::getType, request.getType());
        }
        if (StringUtils.hasText(request.getOrderNo())) {
            wrapper.like(AfterSaleOrder::getOrderNo, request.getOrderNo());
        }
        if (StringUtils.hasText(request.getAfterSaleNo())) {
            wrapper.like(AfterSaleOrder::getAfterSaleNo, request.getAfterSaleNo());
        }
        if (request.getUserId() != null) {
            wrapper.eq(AfterSaleOrder::getUserId, request.getUserId());
        }
        if (request.getShopId() != null) {
            wrapper.eq(AfterSaleOrder::getShopId, request.getShopId());
        }
        if (StringUtils.hasText(request.getPlatform())) {
            wrapper.eq(AfterSaleOrder::getPlatform, request.getPlatform());
        }
        if (StringUtils.hasText(request.getStartTime())) {
            LocalDateTime startTime = LocalDate.parse(request.getStartTime()).atStartOfDay();
            wrapper.ge(AfterSaleOrder::getCreatedAt, startTime);
        }
        if (StringUtils.hasText(request.getEndTime())) {
            LocalDateTime endTime = LocalDate.parse(request.getEndTime()).atTime(LocalTime.MAX);
            wrapper.le(AfterSaleOrder::getCreatedAt, endTime);
        }

        wrapper.orderByDesc(AfterSaleOrder::getCreatedAt);

        Page<AfterSaleOrder> result = afterSaleOrderMapper.selectPage(page, wrapper);

        // 转换响应
        List<AfterSaleListResponse> list = new ArrayList<>();
        for (AfterSaleOrder order : result.getRecords()) {
            AfterSaleListResponse response = new AfterSaleListResponse();
            BeanUtils.copyProperties(order, response);
            response.setTypeName(AfterSaleType.fromCode(order.getType()).getName());
            response.setStatusName(AfterSaleStatus.fromCode(order.getStatus()).getName());
            list.add(response);
        }

        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取售后详情
     */
    public AfterSaleDetailResponse getAfterSaleDetail(Long id) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        AfterSaleDetailResponse response = new AfterSaleDetailResponse();
        BeanUtils.copyProperties(order, response);
        response.setTypeName(AfterSaleType.fromCode(order.getType()).getName());
        response.setStatusName(AfterSaleStatus.fromCode(order.getStatus()).getName());

        // 查询退货地址
        if (order.getReturnAddressId() != null) {
            ReturnAddress address = returnAddressMapper.selectById(order.getReturnAddressId());
            if (address != null) {
                AfterSaleDetailResponse.ReturnAddressVO addressVO = new AfterSaleDetailResponse.ReturnAddressVO();
                addressVO.setReceiverName(address.getReceiverName());
                addressVO.setReceiverPhone(address.getReceiverPhone());
                String fullAddress = address.getProvince() + address.getCity() + 
                        address.getDistrict() + address.getAddress();
                addressVO.setFullAddress(fullAddress);
                response.setReturnAddress(addressVO);
            }
        }

        return response;
    }

    /**
     * 审批售后
     */
    @AuditLog(action = "审批", module = "售后", description = "审批售后申请")
    @Transactional(rollbackFor = Exception.class)
    public void approveAfterSale(Long id, ApproveAfterSaleRequest request, Long approverId, String approverName) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        // 验证状态
        if (!AfterSaleStatus.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有待审核状态的售后单才能审批");
        }

        AfterSaleType type = AfterSaleType.fromCode(order.getType());

        LambdaUpdateWrapper<AfterSaleOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleOrder::getId, id);

        if (request.getApproved()) {
            // 同意
            String newStatus;
            if (type == AfterSaleType.REFUND_ONLY) {
                newStatus = AfterSaleStatus.REFUNDING.getCode();
            } else {
                // 退货退款或换货，需要买家退货
                newStatus = AfterSaleStatus.APPROVED.getCode();

                // 验证退货地址
                if (request.getReturnAddressId() == null) {
                    throw new BusinessException("退货地址不能为空");
                }
                ReturnAddress address = returnAddressMapper.selectById(request.getReturnAddressId());
                if (address == null || !address.getEnabled()) {
                    throw new BusinessException("退货地址不存在或已禁用");
                }
                updateWrapper.set(AfterSaleOrder::getReturnAddressId, request.getReturnAddressId());
            }

            // 调整退款金额时验证上限
            if (request.getRefundAmount() != null && 
                    (type == AfterSaleType.REFUND_ONLY || type == AfterSaleType.RETURN_REFUND)) {
                validateRefundAmount(request.getRefundAmount(), order.getOrderId());
                updateWrapper.set(AfterSaleOrder::getRefundAmount, request.getRefundAmount());
            }

            updateWrapper.set(AfterSaleOrder::getStatus, newStatus);
        } else {
            // 拒绝
            updateWrapper.set(AfterSaleOrder::getStatus, AfterSaleStatus.REJECTED.getCode());
        }

        updateWrapper.set(AfterSaleOrder::getApproverId, approverId);
        updateWrapper.set(AfterSaleOrder::getApproverName, approverName);
        updateWrapper.set(AfterSaleOrder::getApprovedAt, LocalDateTime.now());
        updateWrapper.set(AfterSaleOrder::getApproveRemark, request.getRemark());
        updateWrapper.set(AfterSaleOrder::getUpdatedAt, LocalDateTime.now());

        int rows = afterSaleOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("审批失败");
        }

        log.info("售后审批完成, afterSaleId={}, approved={}, approver={}", 
                id, request.getApproved(), approverName);
    }

    /**
     * 填写退货物流
     */
    @Transactional(rollbackFor = Exception.class)
    public void fillReturnLogistics(Long id, FillLogisticsRequest request) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        // 验证状态
        if (!AfterSaleStatus.APPROVED.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有已同意状态的售后单才能填写退货物流");
        }

        LambdaUpdateWrapper<AfterSaleOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleOrder::getId, id);
        updateWrapper.set(AfterSaleOrder::getStatus, AfterSaleStatus.RETURNING.getCode());
        updateWrapper.set(AfterSaleOrder::getReturnLogisticsCompany, request.getLogisticsCompany());
        updateWrapper.set(AfterSaleOrder::getReturnLogisticsNo, request.getLogisticsNo());
        updateWrapper.set(AfterSaleOrder::getUpdatedAt, LocalDateTime.now());

        int rows = afterSaleOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("填写退货物流失败");
        }

        log.info("退货物流填写成功, afterSaleId={}, logisticsNo={}", id, request.getLogisticsNo());
    }

    /**
     * 确认收货(卖家确认收到退货)
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturnReceived(Long id) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        // 验证状态
        if (!AfterSaleStatus.RETURNING.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有退货中状态的售后单才能确认收货");
        }

        AfterSaleType type = AfterSaleType.fromCode(order.getType());
        String newStatus;
        if (type == AfterSaleType.RETURN_REFUND) {
            newStatus = AfterSaleStatus.REFUNDING.getCode();
        } else if (type == AfterSaleType.EXCHANGE) {
            newStatus = AfterSaleStatus.EXCHANGING.getCode();
        } else {
            throw new BusinessException("售后类型异常");
        }

        LambdaUpdateWrapper<AfterSaleOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleOrder::getId, id);
        updateWrapper.set(AfterSaleOrder::getStatus, newStatus);
        updateWrapper.set(AfterSaleOrder::getReturnedAt, LocalDateTime.now());
        updateWrapper.set(AfterSaleOrder::getUpdatedAt, LocalDateTime.now());

        int rows = afterSaleOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("确认收货失败");
        }

        log.info("退货确认收货成功, afterSaleId={}", id);
    }

    /**
     * 执行退款
     */
    @AuditLog(action = "退款", module = "售后", description = "执行退款操作")
    @Transactional(rollbackFor = Exception.class)
    public void executeRefund(Long id) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        if (!AfterSaleStatus.REFUNDING.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有退款中状态的售后单才能执行退款");
        }

        if (order.getRefundAmount() == null || order.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额无效");
        }

        log.info("开始执行退款, afterSaleId={}, orderId={}, refundAmount={}", 
                id, order.getOrderId(), order.getRefundAmount());

        try {
            PaymentRecord refundRecord = paymentService.executeRefund(
                    id,
                    order.getOrderId(),
                    order.getOrderNo(),
                    order.getUserId(),
                    order.getShopId(),
                    order.getRefundAmount(),
                    order.getReasonDesc()
            );

            LambdaUpdateWrapper<AfterSaleOrder> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AfterSaleOrder::getId, id);
            updateWrapper.set(AfterSaleOrder::getRefundTransactionNo, refundRecord.getTransactionNo());
            updateWrapper.set(AfterSaleOrder::getUpdatedAt, LocalDateTime.now());

            if ("success".equals(refundRecord.getStatus())) {
                updateWrapper.set(AfterSaleOrder::getStatus, AfterSaleStatus.COMPLETED.getCode());
                updateWrapper.set(AfterSaleOrder::getRefundedAt, LocalDateTime.now());
                updateWrapper.set(AfterSaleOrder::getCompletedAt, LocalDateTime.now());
                
                int rows = afterSaleOrderMapper.update(null, updateWrapper);
                if (rows == 0) {
                    throw new BusinessException("更新售后单状态失败");
                }

                log.info("退款执行成功, afterSaleId={}, transactionNo={}", id, refundRecord.getTransactionNo());

                // AS-005: 更新原订单状态为已退款
                try {
                    orderServiceClient.updateOrderStatus(order.getOrderId(), "refunded", "售后退款完成: " + order.getAfterSaleNo());
                    log.info("订单状态已更新为已退款, orderId={}", order.getOrderId());
                } catch (Exception e) {
                    log.warn("更新订单状态失败, orderId={}, error={}", order.getOrderId(), e.getMessage());
                }

                // TODO: 发送退款成功通知给用户
                sendRefundNotification(order, refundRecord, true);

            } else if ("pending".equals(refundRecord.getStatus())) {
                afterSaleOrderMapper.update(null, updateWrapper);
                log.info("退款处理中, afterSaleId={}, transactionNo={}", id, refundRecord.getTransactionNo());

            } else {
                log.error("退款执行失败, afterSaleId={}, reason={}", id, refundRecord.getFailReason());
                throw new BusinessException("退款失败: " + refundRecord.getFailReason());
            }

        } catch (BusinessException e) {
            log.error("退款执行失败, afterSaleId={}, error={}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("退款执行异常, afterSaleId={}", id, e);
            throw new BusinessException("退款执行异常: " + e.getMessage());
        }
    }

    private void sendRefundNotification(AfterSaleOrder order, PaymentRecord refundRecord, boolean success) {
        try {
            // TODO: 集成消息队列或通知服务发送退款通知
            log.info("发送退款通知, orderNo={}, refundAmount={}, success={}", 
                    order.getOrderNo(), refundRecord.getAmount(), success);
        } catch (Exception e) {
            log.warn("发送退款通知失败, orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * 换货发货
     */
    @Transactional(rollbackFor = Exception.class)
    public void shipExchangeGoods(Long id, ExchangeShipRequest request) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        // 验证状态
        if (!AfterSaleStatus.EXCHANGING.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有换货中状态的售后单才能发货");
        }

        LambdaUpdateWrapper<AfterSaleOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleOrder::getId, id);
        updateWrapper.set(AfterSaleOrder::getStatus, AfterSaleStatus.COMPLETED.getCode());
        updateWrapper.set(AfterSaleOrder::getExchangeLogisticsCompany, request.getLogisticsCompany());
        updateWrapper.set(AfterSaleOrder::getExchangeLogisticsNo, request.getLogisticsNo());
        updateWrapper.set(AfterSaleOrder::getCompletedAt, LocalDateTime.now());
        updateWrapper.set(AfterSaleOrder::getUpdatedAt, LocalDateTime.now());

        int rows = afterSaleOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("换货发货失败");
        }

        log.info("换货发货成功, afterSaleId={}, logisticsNo={}", id, request.getLogisticsNo());
    }

    /**
     * 取消售后
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelAfterSale(Long id) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        // 只有待审核状态可以取消
        if (!AfterSaleStatus.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException("只有待审核状态的售后单才能取消");
        }

        LambdaUpdateWrapper<AfterSaleOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleOrder::getId, id);
        updateWrapper.set(AfterSaleOrder::getStatus, AfterSaleStatus.CANCELLED.getCode());
        updateWrapper.set(AfterSaleOrder::getUpdatedAt, LocalDateTime.now());

        int rows = afterSaleOrderMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("取消售后失败");
        }

        log.info("售后取消成功, afterSaleId={}", id);
    }

    /**
     * 添加卖家备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSellerRemark(Long id, String remark) {
        AfterSaleOrder order = afterSaleOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("售后单不存在");
        }

        LambdaUpdateWrapper<AfterSaleOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AfterSaleOrder::getId, id);
        updateWrapper.set(AfterSaleOrder::getSellerRemark, remark);
        updateWrapper.set(AfterSaleOrder::getUpdatedAt, LocalDateTime.now());

        afterSaleOrderMapper.update(null, updateWrapper);
    }

    /**
     * 获取售后原因列表
     */
    public List<AfterSaleReasonResponse> listReasons(String type) {
        LambdaQueryWrapper<AfterSaleReason> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSaleReason::getEnabled, true);
        if (StringUtils.hasText(type)) {
            wrapper.eq(AfterSaleReason::getType, type);
        }
        wrapper.orderByAsc(AfterSaleReason::getSort);

        List<AfterSaleReason> reasons = afterSaleReasonMapper.selectList(wrapper);

        List<AfterSaleReasonResponse> list = new ArrayList<>();
        for (AfterSaleReason reason : reasons) {
            AfterSaleReasonResponse response = new AfterSaleReasonResponse();
            BeanUtils.copyProperties(reason, response);
            list.add(response);
        }

        return list;
    }

    /**
     * 获取售后统计
     */
    public AfterSaleStatisticsResponse getStatistics() {
        AfterSaleStatisticsResponse response = new AfterSaleStatisticsResponse();

        // 统计总数
        LambdaQueryWrapper<AfterSaleOrder> totalWrapper = new LambdaQueryWrapper<>();
        Long totalCount = afterSaleOrderMapper.selectCount(totalWrapper);
        response.setTotalCount(totalCount);

        // 各状态统计
        LambdaQueryWrapper<AfterSaleOrder> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(AfterSaleOrder::getStatus, AfterSaleStatus.PENDING.getCode());
        response.setPendingCount(afterSaleOrderMapper.selectCount(pendingWrapper));

        LambdaQueryWrapper<AfterSaleOrder> processingWrapper = new LambdaQueryWrapper<>();
        processingWrapper.in(AfterSaleOrder::getStatus, 
                AfterSaleStatus.APPROVED.getCode(),
                AfterSaleStatus.RETURNING.getCode(),
                AfterSaleStatus.RETURNED.getCode(),
                AfterSaleStatus.REFUNDING.getCode(),
                AfterSaleStatus.EXCHANGING.getCode());
        response.setProcessingCount(afterSaleOrderMapper.selectCount(processingWrapper));

        LambdaQueryWrapper<AfterSaleOrder> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(AfterSaleOrder::getStatus, AfterSaleStatus.COMPLETED.getCode());
        response.setCompletedCount(afterSaleOrderMapper.selectCount(completedWrapper));

        LambdaQueryWrapper<AfterSaleOrder> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(AfterSaleOrder::getStatus, AfterSaleStatus.REJECTED.getCode());
        response.setRejectedCount(afterSaleOrderMapper.selectCount(rejectedWrapper));

        // 退款总金额
        List<Map<String, Object>> statusCounts = afterSaleOrderMapper.countByStatus();
        List<AfterSaleStatisticsResponse.StatusCount> statusCountList = new ArrayList<>();
        for (Map<String, Object> map : statusCounts) {
            AfterSaleStatisticsResponse.StatusCount sc = new AfterSaleStatisticsResponse.StatusCount();
            sc.setStatus((String) map.get("status"));
            sc.setStatusName(AfterSaleStatus.fromCode((String) map.get("status")).getName());
            sc.setCount(((Number) map.get("count")).longValue());
            statusCountList.add(sc);
        }
        response.setStatusCounts(statusCountList);

        // 各类型统计
        List<Map<String, Object>> typeCounts = afterSaleOrderMapper.countByType();
        List<AfterSaleStatisticsResponse.TypeCount> typeCountList = new ArrayList<>();
        for (Map<String, Object> map : typeCounts) {
            AfterSaleStatisticsResponse.TypeCount tc = new AfterSaleStatisticsResponse.TypeCount();
            tc.setType((String) map.get("type"));
            tc.setTypeName(AfterSaleType.fromCode((String) map.get("type")).getName());
            tc.setCount(((Number) map.get("count")).longValue());
            typeCountList.add(tc);
        }
        response.setTypeCounts(typeCountList);

        // 近7日趋势
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(6).toLocalDate().atStartOfDay();
        List<Map<String, Object>> dailyCounts = afterSaleOrderMapper.countByDate(startTime, endTime);
        List<AfterSaleStatisticsResponse.DailyCount> dailyTrend = new ArrayList<>();
        for (Map<String, Object> map : dailyCounts) {
            AfterSaleStatisticsResponse.DailyCount dc = new AfterSaleStatisticsResponse.DailyCount();
            Object dateObj = map.get("date");
            if (dateObj instanceof java.sql.Date) {
                dc.setDate(((java.sql.Date) dateObj).toLocalDate().toString());
            } else if (dateObj instanceof LocalDateTime) {
                dc.setDate(((LocalDateTime) dateObj).toLocalDate().toString());
            } else {
                dc.setDate(dateObj.toString());
            }
            dc.setCount(((Number) map.get("count")).longValue());
            dailyTrend.add(dc);
        }
        response.setDailyTrend(dailyTrend);

        return response;
    }

    /**
     * 获取退货地址列表
     */
    public List<ReturnAddress> listReturnAddresses(Long shopId) {
        LambdaQueryWrapper<ReturnAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnAddress::getShopId, shopId);
        wrapper.eq(ReturnAddress::getEnabled, true);
        wrapper.orderByDesc(ReturnAddress::getIsDefault);
        return returnAddressMapper.selectList(wrapper);
    }

    /**
     * AS-001: 验证退款金额不超过订单实付金额
     *
     * @param refundAmount 申请退款金额
     * @param orderId      原订单ID
     * @throws BusinessException 如果退款金额超过订单实付金额
     */
    private void validateRefundAmount(BigDecimal refundAmount, Long orderId) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额必须大于0");
        }

        try {
            OrderDTO order = orderServiceClient.getOrderById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在: " + orderId);
            }

            BigDecimal maxRefundAmount = order.getPaidAmount();
            if (maxRefundAmount == null) {
                maxRefundAmount = order.getTotalAmount();
            }

            if (refundAmount.compareTo(maxRefundAmount) > 0) {
                throw new BusinessException(
                    String.format("退款金额 %.2f 不能超过订单实付金额 %.2f", 
                        refundAmount, maxRefundAmount)
                );
            }

            log.debug("退款金额验证通过, refundAmount={}, maxAmount={}", refundAmount, maxRefundAmount);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("获取订单信息失败, orderId={}, error={}", orderId, e.getMessage());
            throw new BusinessException("无法验证退款金额，请稍后重试");
        }
    }
}