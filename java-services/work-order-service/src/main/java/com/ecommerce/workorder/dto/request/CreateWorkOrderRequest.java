package com.ecommerce.workorder.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建工单请求
 */
@Data
@Schema(description = "创建工单请求")
public class CreateWorkOrderRequest {

    @Schema(description = "工单标题", required = true)
    @NotBlank(message = "工单标题不能为空")
    private String title;

    @Schema(description = "工单描述")
    private String description;

    @Schema(description = "工单分类ID", required = true)
    @NotNull(message = "工单分类不能为空")
    private Long categoryId;

    @Schema(description = "优先级: URGENT-紧急, HIGH-高, NORMAL-普通, LOW-低", defaultValue = "NORMAL")
    private String priority = "NORMAL";

    @Schema(description = "关联会话ID")
    private Long sessionId;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "买家名称")
    private String buyerName;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNoRef;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "平台来源")
    private String platform;

    @Schema(description = "来源渠道")
    private String source;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "附件列表")
    private List<String> attachments;

    @Schema(description = "扩展字段")
    private String extra;
}