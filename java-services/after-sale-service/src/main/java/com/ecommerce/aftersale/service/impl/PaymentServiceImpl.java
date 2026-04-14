package com.ecommerce.aftersale.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.ecommerce.aftersale.entity.PaymentRecord;
import com.ecommerce.aftersale.mapper.PaymentRecordMapper;
import com.ecommerce.aftersale.service.PaymentService;
import com.ecommerce.common.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.api.url:http://payment-service:8085}")
    private String paymentApiUrl;

    @Value("${payment.api.timeout:30000}")
    private int timeout;

    @Value("${payment.notify.url:http://after-sale-service:8086/api/after-sale/refund/callback}")
    private String notifyUrl;

    /**
     * 执行退款
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord executeRefund(Long afterSaleId, Long orderId, String orderNo,
                                        Long userId, Long shopId, BigDecimal refundAmount,
                                        String refundReason) {
        log.info("开始执行退款, afterSaleId={}, orderId={}, refundAmount={}", 
                afterSaleId, orderId, refundAmount);

        // 生成退款流水号
        String transactionNo = generateTransactionNo("RF");

        // 创建退款流水记录(处理中状态)
        PaymentRecord record = new PaymentRecord();
        record.setTransactionNo(transactionNo);
        record.setAfterSaleId(afterSaleId);
        record.setOrderId(orderId);
        record.setOrderNo(orderNo);
        record.setUserId(userId);
        record.setShopId(shopId);
        record.setTransactionType("refund");
        record.setAmount(refundAmount);
        record.setStatus("pending");
        record.setRefundReason(refundReason);
        record.setNotifyUrl(notifyUrl);
        record.setTransactionTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        record.setDeleted(0);

        // 保存流水记录
        paymentRecordMapper.insert(record);
        log.info("退款流水记录已创建, transactionNo={}", transactionNo);

        try {
            // 查询原支付记录
            PaymentRecord originalPayment = paymentRecordMapper.selectSuccessfulPaymentByOrderId(orderId);
            if (originalPayment == null) {
                log.error("未找到原支付记录, orderId={}", orderId);
                updateRecordStatus(record, "failed", "未找到原支付记录");
                throw new BusinessException("未找到原支付记录,无法执行退款");
            }

            record.setOriginalTransactionNo(originalPayment.getTransactionNo());
            record.setPaymentChannel(originalPayment.getPaymentChannel());

            // 构建退款请求
            Map<String, Object> refundRequest = new HashMap<>();
            refundRequest.put("transactionNo", transactionNo);
            refundRequest.put("originalTransactionNo", originalPayment.getTransactionNo());
            refundRequest.put("orderId", orderId);
            refundRequest.put("orderNo", orderNo);
            refundRequest.put("userId", userId);
            refundRequest.put("refundAmount", refundAmount);
            refundRequest.put("refundReason", refundReason);
            refundRequest.put("notifyUrl", notifyUrl);

            // 调用支付系统退款接口
            String url = paymentApiUrl + "/api/payment/refund";
            log.info("调用支付系统退款接口, url={}, request={}", url, refundRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(refundRequest, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");
                
                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    if (data != null) {
                        String thirdPartyTransactionNo = (String) data.get("thirdPartyTransactionNo");
                        String status = (String) data.get("status");
                        
                        record.setThirdPartyTransactionNo(thirdPartyTransactionNo);
                        
                        // 根据支付系统返回的状态更新流水状态
                        if ("success".equals(status)) {
                            updateRecordStatus(record, "success", null);
                            log.info("退款成功, transactionNo={}, thirdPartyTransactionNo={}", 
                                    transactionNo, thirdPartyTransactionNo);
                        } else if ("processing".equals(status)) {
                            log.info("退款处理中, transactionNo={}", transactionNo);
                            paymentRecordMapper.updateById(record);
                        } else {
                            String failMsg = (String) data.get("message");
                            updateRecordStatus(record, "failed", failMsg);
                            throw new BusinessException("退款失败: " + failMsg);
                        }
                    }
                } else {
                    String errorMsg = responseBody.get("message") != null ? 
                            responseBody.get("message").toString() : "支付系统返回错误";
                    updateRecordStatus(record, "failed", errorMsg);
                    throw new BusinessException("退款失败: " + errorMsg);
                }
            } else {
                String errorMsg = "支付系统响应异常";
                updateRecordStatus(record, "failed", errorMsg);
                throw new BusinessException(errorMsg);
            }

        } catch (RestClientException e) {
            log.error("调用支付系统失败", e);
            updateRecordStatus(record, "failed", "调用支付系统失败: " + e.getMessage());
            throw new BusinessException("调用支付系统失败: " + e.getMessage());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("退款处理异常", e);
            updateRecordStatus(record, "failed", "退款处理异常: " + e.getMessage());
            throw new BusinessException("退款处理异常: " + e.getMessage());
        }

        return record;
    }

    /**
     * 查询退款状态
     */
    @Override
    public PaymentRecord queryRefundStatus(String transactionNo) {
        PaymentRecord record = paymentRecordMapper.selectByTransactionNo(transactionNo);
        if (record == null) {
            throw new BusinessException("退款流水不存在: " + transactionNo);
        }

        // 如果状态是处理中,则查询支付系统获取最新状态
        if ("pending".equals(record.getStatus())) {
            try {
                String url = paymentApiUrl + "/api/payment/refund/query?transactionNo=" + transactionNo;
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    Integer code = (Integer) responseBody.get("code");
                    
                    if (code != null && code == 200) {
                        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                        if (data != null) {
                            String status = (String) data.get("status");
                            String thirdPartyTransactionNo = (String) data.get("thirdPartyTransactionNo");
                            
                            if (!"pending".equals(status)) {
                                record.setStatus(status);
                                record.setThirdPartyTransactionNo(thirdPartyTransactionNo);
                                if ("success".equals(status)) {
                                    record.setTransactionTime(LocalDateTime.now());
                                } else if ("failed".equals(status)) {
                                    record.setFailReason((String) data.get("message"));
                                }
                                paymentRecordMapper.updateById(record);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("查询退款状态失败, transactionNo={}", transactionNo, e);
            }
        }

        return record;
    }

    /**
     * 根据售后单ID查询退款记录
     */
    @Override
    public List<PaymentRecord> getRefundRecordsByAfterSaleId(Long afterSaleId) {
        return paymentRecordMapper.selectByAfterSaleId(afterSaleId);
    }

    /**
     * 生成流水号
     */
    private String generateTransactionNo(String prefix) {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + dateStr + IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
    }

    /**
     * 更新流水状态
     */
    private void updateRecordStatus(PaymentRecord record, String status, String failReason) {
        record.setStatus(status);
        record.setFailReason(failReason);
        if ("success".equals(status)) {
            record.setTransactionTime(LocalDateTime.now());
        }
        record.setUpdatedAt(LocalDateTime.now());
        paymentRecordMapper.updateById(record);
    }

    /**
     * 处理退款回调
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleRefundCallback(String transactionNo, String status, 
                                      String thirdPartyTransactionNo, String message) {
        log.info("收到退款回调, transactionNo={}, status={}", transactionNo, status);

        PaymentRecord record = paymentRecordMapper.selectByTransactionNo(transactionNo);
        if (record == null) {
            log.warn("退款流水不存在, transactionNo={}", transactionNo);
            return;
        }

        record.setStatus(status);
        record.setThirdPartyTransactionNo(thirdPartyTransactionNo);
        record.setUpdatedAt(LocalDateTime.now());
        
        if ("success".equals(status)) {
            record.setTransactionTime(LocalDateTime.now());
        } else if ("failed".equals(status)) {
            record.setFailReason(message);
        }

        paymentRecordMapper.updateById(record);
        log.info("退款回调处理完成, transactionNo={}, status={}", transactionNo, status);
    }
}