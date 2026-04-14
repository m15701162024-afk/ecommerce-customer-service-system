package com.ecommerce.aftersale.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 换货发货请求
 */
@Data
@Schema(description = "换货发货请求")
public class ExchangeShipRequest {

    @Schema(description = "物流公司", required = true)
    @NotBlank(message = "物流公司不能为空")
    private String logisticsCompany;

    @Schema(description = "物流单号", required = true)
    @NotBlank(message = "物流单号不能为空")
    private String logisticsNo;
}