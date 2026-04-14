package com.ecommerce.workorder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工单解决请求
 */
@Data
@Schema(description = "工单解决请求")
public class ResolveWorkOrderRequest {

    @Schema(description = "工单ID", required = true)
    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;

    @Schema(description = "解决方案", required = true)
    @NotNull(message = "解决方案不能为空")
    private String solution;

    @Schema(description = "处理备注")
    private String remark;
}