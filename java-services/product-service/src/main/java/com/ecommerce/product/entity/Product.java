package com.ecommerce.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@TableName("t_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品分类名称
     */
    private String categoryName;

    /**
     * 商品品牌
     */
    private String brand;

    /**
     * 商品主图URL
     */
    private String mainImage;

    /**
     * 商品图片列表(JSON数组)
     */
    private String images;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品规格(JSON格式)
     */
    private String specifications;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 市场价格
     */
    private BigDecimal marketPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 已售数量
     */
    private Integer soldCount;

    /**
     * 商品状态: 0-下架 1-上架 2-售罄
     */
    private Integer status;

    /**
     * 商品来源: manual-手动录入 1688-1688导入
     */
    private String source;

    /**
     * 关联的1688商品ID
     */
    private String source1688Id;

    /**
     * 1688商品链接
     */
    private String source1688Url;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 商品权重(用于排序)
     */
    private Integer weight;

    /**
     * 是否热门商品
     */
    private Boolean isHot;

    /**
     * 是否新品
     */
    private Boolean isNew;

    /**
     * 是否推荐
     */
    private Boolean isRecommend;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}