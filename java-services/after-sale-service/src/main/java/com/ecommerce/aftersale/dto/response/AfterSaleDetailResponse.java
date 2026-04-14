package com.ecommerce.aftersale.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后详情响应
 */
@Data
@Schema(description = "售后详情响应")
public class AfterSaleDetailResponse {

    @Schema(description = "售后单ID")
    private Long id;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "原订单ID")
    private Long orderId;

    @Schema(description = "原订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "店铺ID")
    private Long shopId;

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

    @Schema(description = "售后原因ID")
    private Long reasonId;

    @Schema(description = "售后原因描述")
    private String reasonDesc;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "凭证图片")
    private List<String> evidenceImages;

    // 退货物流信息
    @Schema(description = "退货物流公司")
    private String returnLogisticsCompany;

    @Schema(description = "退货物流单号")
    private String returnLogisticsNo;

    @Schema(description = "退货时间")
    private LocalDateTime returnedAt;

    @Schema(description = "退货地址ID")
    private Long returnAddressId;

    // 退货地址详情
    @Schema(description = "退货地址")
    private ReturnAddressVO returnAddress;

    // 换货信息
    @Schema(description = "换货商品SKU")
    private String exchangeSku;

    @Schema(description = "换货数量")
    private Integer exchangeQuantity;

    @Schema(description = "换货发货物流公司")
    private String exchangeLogisticsCompany;

    @Schema(description = "换货发货物流单号")
    private String exchangeLogisticsNo;

    // 审批信息
    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批时间")
    private LocalDateTime approvedAt;

    @Schema(description = "审批备注")
    private String approveRemark;

    // 退款信息
    @Schema(description = "退款时间")
    private LocalDateTime refundedAt;

    @Schema(description = "退款流水号")
    private String refundTransactionNo;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    // 商品信息
    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品SKU")
    private String productSku;

    @Schema(description = "商品数量")
    private Integer quantity;

    @Schema(description = "买家备注")
    private String buyerRemark;

    @Schema(description = "卖家备注")
    private String sellerRemark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 退货地址VO
     */
    @Data
    @Schema(description = "退货地址")
    public static class ReturnAddressVO {
        @Schema(description = "收货人姓名")
        private String receiverName;

        @Schema(description = "收货人电话")
        private String receiverPhone;

        @Schema(description = "完整地址")
        private String fullAddress;
    }
}