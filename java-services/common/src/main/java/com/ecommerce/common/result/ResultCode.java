package com.ecommerce.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    FAILED(400, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // 业务错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务状态码 1xxx
    SHOP_NOT_FOUND(1001, "店铺不存在"),
    SHOP_DISABLED(1002, "店铺已禁用"),
    PRODUCT_NOT_FOUND(1011, "商品不存在"),
    PRODUCT_OFF_SHELF(1012, "商品已下架"),
    ORDER_NOT_FOUND(1021, "订单不存在"),
    ORDER_STATUS_ERROR(1022, "订单状态错误"),
    ORDER_ALREADY_PAID(1023, "订单已支付"),
    ORDER_ALREADY_SHIPPED(1024, "订单已发货"),
    
    // 平台错误 2xxx
    PLATFORM_AUTH_FAILED(2001, "平台授权失败"),
    PLATFORM_TOKEN_EXPIRED(2002, "平台令牌已过期"),
    PLATFORM_API_ERROR(2003, "平台API调用失败"),
    DOUYIN_API_ERROR(2011, "抖音API调用失败"),
    TAOBAO_API_ERROR(2021, "淘宝API调用失败"),
    XIAOHONGSHU_API_ERROR(2031, "小红书API调用失败"),
    A1688_API_ERROR(2041, "1688 API调用失败"),
    
    // 采购错误 3xxx
    SUPPLIER_NOT_FOUND(3001, "供应商不存在"),
    PURCHASE_ORDER_NOT_FOUND(3002, "采购单不存在"),
    PURCHASE_PAYMENT_FAILED(3003, "采购支付失败"),
    PURCHASE_NEED_MANUAL_CONFIRM(3004, "采购需要人工确认"),
    
    // 支付错误 4xxx
    PAYMENT_FAILED(4001, "支付失败"),
    PAYMENT_TIMEOUT(4002, "支付超时"),
    PAYMENT_AMOUNT_ERROR(4003, "支付金额错误"),
    
    // 风控错误 5xxx
    RISK_BLOCKED(5001, "操作被风控拦截"),
    RISK_NEED_REVIEW(5002, "操作需要人工审核"),
    
    // 客服错误 6xxx
    SESSION_NOT_FOUND(6001, "会话不存在"),
    MESSAGE_SEND_FAILED(6002, "消息发送失败"),
    AI_SERVICE_ERROR(6003, "AI服务异常");
    
    private final Integer code;
    private final String message;
}