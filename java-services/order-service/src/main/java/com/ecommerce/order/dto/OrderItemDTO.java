package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单商品项DTO
 */
@Data
@Schema(description = "订单商品项")
public class OrderItemDTO {

    @Schema(description = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "SKU编码", required = true)
    @NotNull(message = "SKU不能为空")
    private String sku;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "购买数量", required = true)
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;

    @Schema(description = "商品单价")
    private BigDecimal unitPrice;

    @Schema(description = "商品总金额")
    private BigDecimal totalAmount;
}