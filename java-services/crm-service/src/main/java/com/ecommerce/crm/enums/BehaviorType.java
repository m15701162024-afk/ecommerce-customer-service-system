package com.ecommerce.crm.enums;

import lombok.Getter;

/**
 * 行为类型枚举
 */
@Getter
public enum BehaviorType {
    
    LOGIN("LOGIN", "登录"),
    VIEW_PRODUCT("VIEW_PRODUCT", "浏览商品"),
    ADD_CART("ADD_CART", "加入购物车"),
    PLACE_ORDER("PLACE_ORDER", "下单"),
    PAYMENT("PAYMENT", "支付"),
    CANCEL_ORDER("CANCEL_ORDER", "取消订单"),
    REFUND("REFUND", "退款"),
    CONSULTATION("CONSULTATION", "咨询"),
    COMPLAINT("COMPLAINT", "投诉"),
    REVIEW("REVIEW", "评价"),
    FAVORITE("FAVORITE", "收藏"),
    SHARE("SHARE", "分享");
    
    private final String code;
    private final String description;
    
    BehaviorType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static BehaviorType fromCode(String code) {
        for (BehaviorType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}