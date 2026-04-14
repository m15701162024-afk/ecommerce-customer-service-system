package com.ecommerce.workorder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单分配请求
 */
@Data
@Schema(description = "工单分配请求")
public class AssignWorkOrderRequest {

    @Schema(description = "工单ID", required = true)
    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;

    @Schema(description = "处理人ID", required = true)
    @NotNull(message = "处理人ID不能为空")
    private Long assigneeId;

    @Schema(description = "处理人名称")
    private String assigneeName;

    @Schema(description = "团队ID")
    private Long teamId;

    @Schema(description = "分配备注")
    private String remark;
}