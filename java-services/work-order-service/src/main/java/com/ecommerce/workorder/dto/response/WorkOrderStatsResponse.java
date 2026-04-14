package com.ecommerce.workorder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 工单统计响应
 */
@Data
@Schema(description = "工单统计响应")
public class WorkOrderStatsResponse {

    @Schema(description = "总工单数")
    private Long totalCount;

    @Schema(description = "待处理数")
    private Long pendingCount;

    @Schema(description = "处理中数")
    private Long processingCount;

    @Schema(description = "已解决数")
    private Long resolvedCount;

    @Schema(description = "已关闭数")
    private Long closedCount;

    @Schema(description = "超时工单数")
    private Long overdueCount;

    @Schema(description = "今日新增数")
    private Long todayCreatedCount;

    @Schema(description = "今日解决数")
    private Long todayResolvedCount;

    @Schema(description = "平均响应时长(分钟)")
    private Double avgResponseTime;

    @Schema(description = "平均解决时长(分钟)")
    private Double avgResolveTime;

    @Schema(description = "按时响应率(%)")
    private Double onTimeResponseRate;

    @Schema(description = "按时解决率(%)")
    private Double onTimeResolveRate;

    @Schema(description = "满意度评分")
    private Double avgSatisfactionScore;

    @Schema(description = "按状态统计")
    private List<Map<String, Object>> statusStats;

    @Schema(description = "按优先级统计")
    private List<Map<String, Object>> priorityStats;

    @Schema(description = "按分类统计")
    private List<Map<String, Object>> categoryStats;

    @Schema(description = "客服工单统计")
    private List<Map<String, Object>> assigneeStats;
}