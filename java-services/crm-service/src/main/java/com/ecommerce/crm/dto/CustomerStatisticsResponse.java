package com.ecommerce.crm.dto;

import com.ecommerce.crm.enums.CustomerActivity;
import com.ecommerce.crm.enums.CustomerLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 客户统计响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatisticsResponse {
    
    // 基础统计
    private Long totalCustomers;
    private Long enabledCustomers;
    private Long blacklistCustomers;
    
    // 等级分布
    private List<LevelDistribution> levelDistribution;
    
    // 活跃度分布
    private List<ActivityDistribution> activityDistribution;
    
    // 来源分布
    private List<SourceDistribution> sourceDistribution;
    
    // 消费统计
    private BigDecimal totalAmount;
    private BigDecimal avgAmount;
    private BigDecimal maxAmount;
    
    // 新增统计
    private Long todayNewCustomers;
    private Long weekNewCustomers;
    private Long monthNewCustomers;
    
    // RFM分析
    private RfmAnalysis rfmAnalysis;
    
    /**
     * 等级分布
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelDistribution {
        private CustomerLevel level;
        private String levelName;
        private Long count;
        private BigDecimal percentage;
    }
    
    /**
     * 活跃度分布
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDistribution {
        private CustomerActivity activity;
        private String activityName;
        private Long count;
        private BigDecimal percentage;
    }
    
    /**
     * 来源分布
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDistribution {
        private String source;
        private Long count;
        private BigDecimal percentage;
    }
    
    /**
     * RFM分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RfmAnalysis {
        // R-最近一次消费时间
        private Long recentCustomers;      // 7天内
        private Long activeCustomers;      // 30天内
        private Long inactiveCustomers;    // 90天以上
        
        // F-消费频率
        private Long highFrequency;        // 10次以上
        private Long mediumFrequency;      // 5-10次
        private Long lowFrequency;         // 5次以下
        
        // M-消费金额
        private Long highValue;            // 10000以上
        private Long mediumValue;          // 3000-10000
        private Long lowValue;             // 3000以下
        
        // 客户分层
        private Long vipCustomers;         // 重要价值客户
        private Long loyalCustomers;       // 重要保持客户
        private Long newCustomers;         // 新客户
        private Long lostCustomers;        // 流失客户
    }
}