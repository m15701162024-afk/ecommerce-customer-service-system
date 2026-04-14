package com.ecommerce.crm.enums;

import lombok.Getter;

/**
 * 客户活跃度枚举
 */
@Getter
public enum CustomerActivity {
    
    INACTIVE("INACTIVE", "不活跃", 0),
    LOW("LOW", "低活跃", 1),
    NORMAL("NORMAL", "一般", 2),
    ACTIVE("ACTIVE", "活跃", 3),
    HIGHLY_ACTIVE("HIGHLY_ACTIVE", "高活跃", 4);
    
    private final String code;
    private final String description;
    private final int level;
    
    CustomerActivity(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }
    
    public static CustomerActivity fromCode(String code) {
        for (CustomerActivity activity : values()) {
            if (activity.getCode().equals(code)) {
                return activity;
            }
        }
        return NORMAL;
    }
    
    /**
     * 根据最近活跃天数计算活跃度
     */
    public static CustomerActivity calculateByDays(Integer daysSinceLastActive) {
        if (daysSinceLastActive == null || daysSinceLastActive > 90) {
            return INACTIVE;
        } else if (daysSinceLastActive > 60) {
            return LOW;
        } else if (daysSinceLastActive > 30) {
            return NORMAL;
        } else if (daysSinceLastActive > 7) {
            return ACTIVE;
        }
        return HIGHLY_ACTIVE;
    }
}