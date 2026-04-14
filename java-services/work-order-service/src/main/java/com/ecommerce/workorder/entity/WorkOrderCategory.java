package com.ecommerce.workorder.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_work_order_category")
public class WorkOrderCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类层级
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 默认SLA响应时效(小时)
     */
    private Integer defaultResponseTime;

    /**
     * 默认SLA解决时效(小时)
     */
    private Integer defaultResolveTime;

    /**
     * 默认优先级
     */
    private String defaultPriority;

    /**
     * 负责团队ID
     */
    private Long teamId;

    /**
     * 是否启用
     */
    private Boolean enabled;
}