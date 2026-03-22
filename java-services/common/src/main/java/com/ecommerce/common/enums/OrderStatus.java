package com.ecommerce.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    
    PENDING("pending", "待支付"),
    PAID("paid", "已支付"),
    PREPARING("preparing", "备货中"),
    SHIPPED("shipped", "已发货"),
    DELIVERED("delivered", "已签收"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消"),
    REFUNDING("refunding", "退款中"),
    REFUNDED("refunded", "已退款");
    
    private final String code;
    private final String name;
    
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + code);
    }
}