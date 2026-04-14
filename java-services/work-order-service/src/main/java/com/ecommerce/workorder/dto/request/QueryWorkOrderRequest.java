package com.ecommerce.workorder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单查询请求
 */
@Data
@Schema(description = "工单查询请求")
public class QueryWorkOrderRequest {

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "工单编号")
    private String orderNo;

    @Schema(description = "工单状态: PENDING, ASSIGNED, PROCESSING, RESOLVED, CLOSED, REOPENED")
    private String status;

    @Schema(description = "优先级: URGENT, HIGH, NORMAL, LOW")
    private String priority;

    @Schema(description = "工单分类ID")
    private Long categoryId;

    @Schema(description = "当前处理人ID")
    private Long assigneeId;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "所属团队ID")
    private Long teamId;

    @Schema(description = "关键词搜索(标题/描述)")
    private String keyword;

    @Schema(description = "创建时间开始")
    private LocalDateTime createdAtStart;

    @Schema(description = "创建时间结束")
    private LocalDateTime createdAtEnd;

    @Schema(description = "是否超时")
    private Boolean overdue;

    @Schema(description = "是否已响应")
    private Boolean responded;
}