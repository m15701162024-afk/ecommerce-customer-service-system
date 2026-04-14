package com.ecommerce.workorder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单转派请求
 */
@Data
@Schema(description = "工单转派请求")
public class TransferWorkOrderRequest {

    @Schema(description = "工单ID", required = true)
    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;

    @Schema(description = "新处理人ID", required = true)
    @NotNull(message = "新处理人ID不能为空")
    private Long newAssigneeId;

    @Schema(description = "新处理人名称")
    private String newAssigneeName;

    @Schema(description = "新团队ID")
    private Long newTeamId;

    @Schema(description = "转派原因", required = true)
    @NotNull(message = "转派原因不能为空")
    private String reason;
}