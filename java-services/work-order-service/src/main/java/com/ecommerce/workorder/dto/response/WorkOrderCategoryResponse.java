package com.ecommerce.workorder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工单分类响应
 */
@Data
@Schema(description = "工单分类响应")
public class WorkOrderCategoryResponse {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "分类层级")
    private Integer level;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "默认SLA响应时效(小时)")
    private Integer defaultResponseTime;

    @Schema(description = "默认SLA解决时效(小时)")
    private Integer defaultResolveTime;

    @Schema(description = "默认优先级")
    private String defaultPriority;

    @Schema(description = "负责团队ID")
    private Long teamId;

    @Schema(description = "是否启用")
    private Boolean enabled;
}