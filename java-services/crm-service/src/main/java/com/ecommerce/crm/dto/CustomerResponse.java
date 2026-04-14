package com.ecommerce.crm.dto;

import com.ecommerce.crm.enums.CustomerActivity;
import com.ecommerce.crm.enums.CustomerLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 客户详情响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    
    private Long id;
    
    // 基本信息
    private String name;
    private String phone;
    private String email;
    private String wechat;
    private String avatar;
    private String gender;
    private LocalDate birthday;
    private String address;
    private String province;
    private String city;
    private String district;
    
    // 来源信息
    private String source;
    private String externalId;
    private Long storeId;
    
    // 客户等级
    private CustomerLevel level;
    private String levelDescription;
    
    // 消费统计
    private Integer totalOrders;
    private BigDecimal totalAmount;
    private BigDecimal avgOrderAmount;
    private BigDecimal maxOrderAmount;
    private BigDecimal refundAmount;
    private Integer refundCount;
    
    // 活跃度
    private CustomerActivity activity;
    private String activityDescription;
    private LocalDateTime lastOrderTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastConsultTime;
    private Integer loginCount;
    private Integer consultCount;
    
    // 画像
    private String consumptionPreference;
    private String priceSensitivity;
    private String brandPreference;
    private String remark;
    
    // 标签
    private Set<TagInfo> tags;
    
    // 分组
    private GroupInfo group;
    
    // RFM评分
    private Integer rfmScore;
    
    // 状态
    private Boolean enabled;
    private Boolean isBlacklist;
    
    // 客服
    private Long serviceStaffId;
    private String serviceStaffName;
    
    // 时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 标签信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagInfo {
        private Long id;
        private String name;
        private String code;
        private String category;
        private String color;
    }
    
    /**
     * 分组信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo {
        private Long id;
        private String name;
        private String code;
    }
}