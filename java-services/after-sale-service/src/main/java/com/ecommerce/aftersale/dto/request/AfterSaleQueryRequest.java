package com.ecommerce.aftersale.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 售后查询请求
 */
@Data
@Schema(description = "售后查询请求")
public class AfterSaleQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "售后状态")
    private String status;

    @Schema(description = "售后类型")
    private String type;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}