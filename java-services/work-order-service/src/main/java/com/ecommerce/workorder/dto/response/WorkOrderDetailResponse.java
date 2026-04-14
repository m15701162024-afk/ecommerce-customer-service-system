package com.ecommerce.workorder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单详情响应
 */
@Data
@Schema(description = "工单详情响应")
public class WorkOrderDetailResponse {

    @Schema(description = "工单ID")
    private Long id;

    @Schema(description = "工单编号")
    private String orderNo;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "工单描述")
    private String description;

    @Schema(description = "工单状态")
    private String status;

    @Schema(description = "优先级")
    private String priority;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "关联会话ID")
    private Long sessionId;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNoRef;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "当前处理人ID")
    private Long assigneeId;

    @Schema(description = "当前处理人名称")
    private String assigneeName;

    @Schema(description = "所属团队ID")
    private Long teamId;

    @Schema(description = "SLA响应截止时间")
    private LocalDateTime slaResponseTime;

    @Schema(description = "SLA解决截止时间")
    private LocalDateTime slaResolveTime;

    @Schema(description = "首次响应时间")
    private LocalDateTime firstResponseTime;

    @Schema(description = "解决时间")
    private LocalDateTime resolvedAt;

    @Schema(description = "关闭时间")
    private LocalDateTime closedAt;

    @Schema(description = "解决方案")
    private String solution;

    @Schema(description = "满意度评分(1-5)")
    private Integer satisfactionScore;

    @Schema(description = "评价内容")
    private String feedback;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "附件列表")
    private List<String> attachments;

    @Schema(description = "来源渠道")
    private String source;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "是否超时")
    private Boolean overdue;

    @Schema(description = "流转记录")
    private List<WorkOrderFlowResponse> flows;
}