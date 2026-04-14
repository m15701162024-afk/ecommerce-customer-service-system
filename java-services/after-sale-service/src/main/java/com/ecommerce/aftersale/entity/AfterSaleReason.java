package com.ecommerce.aftersale.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 售后原因实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_after_sale_reason")
public class AfterSaleReason extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 原因编码
     */
    private String code;

    /**
     * 原因名称
     */
    private String name;

    /**
     * 原因类型: refund-退款, return-退货, exchange-换货
     */
    private String type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;
}