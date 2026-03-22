package com.ecommerce.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum Platform {
    
    DOUYIN("douyin", "抖音"),
    TAOBAO("taobao", "淘宝"),
    XIAOHONGSHU("xiaohongshu", "小红书"),
    A1688("1688", "1688");
    
    private final String code;
    private final String name;
    
    public static Platform fromCode(String code) {
        for (Platform platform : values()) {
            if (platform.getCode().equalsIgnoreCase(code)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unknown platform: " + code);
    }
}