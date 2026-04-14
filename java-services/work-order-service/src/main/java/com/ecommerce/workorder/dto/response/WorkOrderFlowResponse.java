package com.ecommerce.workorder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单流转记录响应
 */
@Data
@Schema(description = "工单流转记录响应")
public class WorkOrderFlowResponse {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作前状态")
    private String fromStatus;

    @Schema(description = "操作后状态")
    private String toStatus;

    @Schema(description = "操作前处理人名称")
    private String fromAssigneeName;

    @Schema(description = "操作后处理人名称")
    private String toAssigneeName;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作备注")
    private String remark;

    @Schema(description = "操作时间")
    private LocalDateTime createdAt;
}