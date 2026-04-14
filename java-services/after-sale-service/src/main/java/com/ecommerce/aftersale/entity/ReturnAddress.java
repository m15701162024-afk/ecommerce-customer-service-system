package com.ecommerce.aftersale.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ecommerce.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 退货地址实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_return_address")
public class ReturnAddress extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 是否默认
     */
    private Boolean isDefault;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;
}