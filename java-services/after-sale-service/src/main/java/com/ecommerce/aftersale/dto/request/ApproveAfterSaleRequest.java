package com.ecommerce.aftersale.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批售后请求
 */
@Data
@Schema(description = "审批售后请求")
public class ApproveAfterSaleRequest {

    @Schema(description = "是否同意", required = true)
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    @Schema(description = "审批备注")
    private String remark;

    @Schema(description = "退货地址ID(同意退货退款/换货时需要)")
    private Long returnAddressId;

    @Schema(description = "退款金额(同意时可以调整)")
    private java.math.BigDecimal refundAmount;
}