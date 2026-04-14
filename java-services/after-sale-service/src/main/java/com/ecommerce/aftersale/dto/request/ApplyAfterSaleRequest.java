package com.ecommerce.aftersale.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 申请售后请求
 */
@Data
@Schema(description = "申请售后请求")
public class ApplyAfterSaleRequest {

    @Schema(description = "原订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "原订单编号", required = true)
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @Schema(description = "售后类型: refund_only-仅退款, return_refund-退货退款, exchange-换货", required = true)
    @NotBlank(message = "售后类型不能为空")
    private String type;

    @Schema(description = "售后原因ID", required = true)
    @NotNull(message = "售后原因不能为空")
    private Long reasonId;

    @Schema(description = "售后原因描述")
    private String reasonDesc;

    @Schema(description = "退款金额(仅退款和退货退款时必填)")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "凭证图片")
    private List<String> evidenceImages;

    @Schema(description = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品SKU")
    private String productSku;

    @Schema(description = "商品数量", required = true)
    @NotNull @Positive(message = "商品数量必须大于0")
    private Integer quantity;

    @Schema(description = "换货SKU(换货时必填)")
    private String exchangeSku;

    @Schema(description = "换货数量(换货时必填)")
    private Integer exchangeQuantity;

    @Schema(description = "买家备注")
    private String buyerRemark;
}