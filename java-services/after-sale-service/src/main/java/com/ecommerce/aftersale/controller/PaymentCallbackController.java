package com.ecommerce.aftersale.controller;

import com.ecommerce.aftersale.service.impl.PaymentServiceImpl;
import com.ecommerce.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付回调控制器
 */
@Slf4j
@Tag(name = "支付回调", description = "支付系统回调接口")
@RestController
@RequestMapping("/api/after-sale/refund")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PaymentServiceImpl paymentServiceImpl;

    @Operation(summary = "退款回调", description = "支付系统退款结果回调")
    @PostMapping("/callback")
    public Result<Void> handleRefundCallback(@RequestBody Map<String, Object> callback) {
        String transactionNo = (String) callback.get("transactionNo");
        String status = (String) callback.get("status");
        String thirdPartyTransactionNo = (String) callback.get("thirdPartyTransactionNo");
        String message = (String) callback.get("message");

        log.info("收到退款回调, transactionNo={}, status={}", transactionNo, status);

        if (transactionNo == null || status == null) {
            log.warn("退款回调参数无效: {}", callback);
            return Result.success();
        }

        paymentServiceImpl.handleRefundCallback(transactionNo, status, thirdPartyTransactionNo, message);

        return Result.success();
    }
}