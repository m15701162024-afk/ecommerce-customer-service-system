package com.ecommerce.aftersale.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 售后统计响应
 */
@Data
@Schema(description = "售后统计响应")
public class AfterSaleStatisticsResponse {

    @Schema(description = "总售后单数")
    private Long totalCount;

    @Schema(description = "待审核数")
    private Long pendingCount;

    @Schema(description = "处理中数")
    private Long processingCount;

    @Schema(description = "已完成数")
    private Long completedCount;

    @Schema(description = "已拒绝数")
    private Long rejectedCount;

    @Schema(description = "退款总金额")
    private java.math.BigDecimal totalRefundAmount;

    @Schema(description = "各状态统计")
    private List<StatusCount> statusCounts;

    @Schema(description = "各类型统计")
    private List<TypeCount> typeCounts;

    @Schema(description = "近7日趋势")
    private List<DailyCount> dailyTrend;

    /**
     * 状态统计
     */
    @Data
    @Schema(description = "状态统计")
    public static class StatusCount {
        @Schema(description = "状态编码")
        private String status;

        @Schema(description = "状态名称")
        private String statusName;

        @Schema(description = "数量")
        private Long count;
    }

    /**
     * 类型统计
     */
    @Data
    @Schema(description = "类型统计")
    public static class TypeCount {
        @Schema(description = "类型编码")
        private String type;

        @Schema(description = "类型名称")
        private String typeName;

        @Schema(description = "数量")
        private Long count;
    }

    /**
     * 每日统计
     */
    @Data
    @Schema(description = "每日统计")
    public static class DailyCount {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "数量")
        private Long count;
    }
}