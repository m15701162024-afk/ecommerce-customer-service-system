package com.ecommerce.crm.enums;

import lombok.Getter;

/**
 * 客户等级枚举
 */
@Getter
public enum CustomerLevel {
    
    NORMAL("NORMAL", "普通客户", 0),
    SILVER("SILVER", "银卡会员", 1),
    GOLD("GOLD", "金卡会员", 2),
    PLATINUM("PLATINUM", "白金会员", 3),
    DIAMOND("DIAMOND", "钻石会员", 4);
    
    private final String code;
    private final String description;
    private final int level;
    
    CustomerLevel(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }
    
    public static CustomerLevel fromCode(String code) {
        for (CustomerLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return NORMAL;
    }
    
    /**
     * 根据消费金额计算客户等级
     */
    public static CustomerLevel calculateByAmount(java.math.BigDecimal totalAmount) {
        if (totalAmount == null) {
            return NORMAL;
        }
        double amount = totalAmount.doubleValue();
        if (amount >= 50000) {
            return DIAMOND;
        } else if (amount >= 20000) {
            return PLATINUM;
        } else if (amount >= 10000) {
            return GOLD;
        } else if (amount >= 3000) {
            return SILVER;
        }
        return NORMAL;
    }
}