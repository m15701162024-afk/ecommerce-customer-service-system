package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateRequest {

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "店铺ID", required = true)
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "订单商品项列表", required = true)
    @NotEmpty(message = "订单商品项不能为空")
    @Valid
    private List<OrderItemDTO> items;

    @Schema(description = "运费")
    private BigDecimal freightAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "收货人姓名", required = true)
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @Schema(description = "收货人电话", required = true)
    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    @Schema(description = "收货地址", required = true)
    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    @Schema(description = "买家备注")
    private String buyerRemark;

    @Schema(description = "外部订单号（用于平台同步）")
    private String externalOrderNo;
}