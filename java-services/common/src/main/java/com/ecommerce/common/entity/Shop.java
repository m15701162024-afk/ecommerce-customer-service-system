package com.ecommerce.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 店铺实体
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop")
public class Shop extends BaseEntity {

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 平台: DOUYIN/TAOBAO/XIAOHONGSHU/A1688
     */
    private String platform;

    /**
     * 平台店铺ID
     */
    private String platformShopId;

    /**
     * 店主用户ID
     */
    private Long ownerId;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 平台访问令牌(加密)
     */
    private String accessToken;

    /**
     * 刷新令牌(加密)
     */
    private String refreshToken;

    /**
     * 令牌过期时间
     */
    private LocalDateTime tokenExpireTime;
}