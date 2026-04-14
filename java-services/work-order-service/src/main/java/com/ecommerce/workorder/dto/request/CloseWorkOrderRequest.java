package com.ecommerce.workorder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单关闭请求
 */
@Data
@Schema(description = "工单关闭请求")
public class CloseWorkOrderRequest {

    @Schema(description = "工单ID", required = true)
    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;

    @Schema(description = "关闭原因")
    private String reason;

    @Schema(description = "满意度评分(1-5)")
    private Integer satisfactionScore;

    @Schema(description = "评价内容")
    private String feedback;
}