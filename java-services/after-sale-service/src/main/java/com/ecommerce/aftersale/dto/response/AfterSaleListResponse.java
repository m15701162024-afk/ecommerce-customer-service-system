package com.ecommerce.aftersale.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后列表项响应
 */
@Data
@Schema(description = "售后列表项响应")
public class AfterSaleListResponse {

    @Schema(description = "售后单ID")
    private Long id;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "原订单编号")
    private String orderNo;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "售后类型")
    private String type;

    @Schema(description = "售后类型名称")
    private String typeName;

    @Schema(description = "售后状态")
    private String status;

    @Schema(description = "售后状态名称")
    private String statusName;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品数量")
    private Integer quantity;

    @Schema(description = "售后原因")
    private String reasonDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "审批时间")
    private LocalDateTime approvedAt;
}