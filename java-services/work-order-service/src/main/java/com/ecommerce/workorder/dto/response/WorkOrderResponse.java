package com.ecommerce.workorder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单响应
 */
@Data
@Schema(description = "工单响应")
public class WorkOrderResponse {

    @Schema(description = "工单ID")
    private Long id;

    @Schema(description = "工单编号")
    private String orderNo;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "工单状态")
    private String status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "当前处理人ID")
    private Long assigneeId;

    @Schema(description = "当前处理人名称")
    private String assigneeName;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "SLA响应截止时间")
    private LocalDateTime slaResponseTime;

    @Schema(description = "SLA解决截止时间")
    private LocalDateTime slaResolveTime;

    @Schema(description = "是否超时")
    private Boolean overdue;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "首次响应时间")
    private LocalDateTime firstResponseTime;

    @Schema(description = "解决时间")
    private LocalDateTime resolvedAt;
}