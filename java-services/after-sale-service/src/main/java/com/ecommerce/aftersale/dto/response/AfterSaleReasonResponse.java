package com.ecommerce.aftersale.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 售后原因响应
 */
@Data
@Schema(description = "售后原因响应")
public class AfterSaleReasonResponse {

    @Schema(description = "原因ID")
    private Long id;

    @Schema(description = "原因编码")
    private String code;

    @Schema(description = "原因名称")
    private String name;

    @Schema(description = "原因类型")
    private String type;

    @Schema(description = "排序")
    private Integer sort;
}