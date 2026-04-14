package com.ecommerce.aftersale.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 售后状态枚举
 * 
 * 状态流转:
 * 仅退款: PENDING -> APPROVED -> REFUNDING -> COMPLETED / REJECTED
 * 退货退款: PENDING -> APPROVED -> RETURNING -> RETURNED -> REFUNDING -> COMPLETED / REJECTED
 * 换货: PENDING -> APPROVED -> RETURNING -> RETURNED -> EXCHANGING -> COMPLETED / REJECTED
 */
@Getter
@AllArgsConstructor
public enum AfterSaleStatus {

    PENDING("pending", "待审核"),
    APPROVED("approved", "已同意"),
    REJECTED("rejected", "已拒绝"),
    RETURNING("returning", "退货中"),
    RETURNED("returned", "已退货"),
    REFUNDING("refunding", "退款中"),
    EXCHANGING("exchanging", "换货中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String name;

    public static AfterSaleStatus fromCode(String code) {
        for (AfterSaleStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown after sale status: " + code);
    }
}