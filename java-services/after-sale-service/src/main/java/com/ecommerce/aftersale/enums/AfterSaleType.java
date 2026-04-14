package com.ecommerce.aftersale.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 售后类型枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleType {

    REFUND_ONLY("refund_only", "仅退款"),
    RETURN_REFUND("return_refund", "退货退款"),
    EXCHANGE("exchange", "换货");

    private final String code;
    private final String name;

    public static AfterSaleType fromCode(String code) {
        for (AfterSaleType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown after sale type: " + code);
    }
}