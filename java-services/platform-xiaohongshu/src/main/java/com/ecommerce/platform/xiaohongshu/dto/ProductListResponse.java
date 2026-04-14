package com.ecommerce.platform.xiaohongshu.dto;

import lombok.Data;
import java.util.List;

/**
 * 小红书商品列表响应
 */
@Data
public class ProductListResponse {
    /**
     * 错误码，0表示成功
     */
    private Integer code;
    
    /**
     * 错误信息
     */
    private String msg;
    
    /**
     * 响应数据
     */
    private Data data;
    
    @Data
    public static class Data {
        /**
         * 商品列表
         */
        private List<ProductInfo> list;
        
        /**
         * 总数
         */
        private Long total;
        
        /**
         * 当前页码
         */
        private Integer page;
        
        /**
         * 每页数量
         */
        private Integer pageSize;
        
        /**
         * 是否有更多
         */
        private Boolean hasMore;
    }
    
    @Data
    public static class ProductInfo {
        /**
         * 商品ID
         */
        private String productId;
        
        /**
         * 商品名称
         */
        private String productName;
        
        /**
         * 商品主图
         */
        private String mainImage;
        
        /**
         * 商品图片列表
         */
        private List<String> images;
        
        /**
         * 商品描述
         */
        private String description;
        
        /**
         * 商品状态: 1-上架, 2-下架, 3-审核中, 4-审核失败
         */
        private Integer status;
        
        /**
         * 商品价格(分)
         */
        private Long price;
        
        /**
         * 市场价(分)
         */
        private Long marketPrice;
        
        /**
         * 库存
         */
        private Integer stock;
        
        /**
         * 销量
         */
        private Integer sales;
        
        /**
         * SKU列表
         */
        private List<SkuInfo> skuList;
        
        /**
         * 商品类目ID
         */
        private String categoryId;
        
        /**
         * 商品类目名称
         */
        private String categoryName;
        
        /**
         * 创建时间
         */
        private Long createTime;
        
        /**
         * 更新时间
         */
        private Long updateTime;
    }
    
    @Data
    public static class SkuInfo {
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * SKU名称
         */
        private String skuName;
        
        /**
         * SKU价格(分)
         */
        private Long price;
        
        /**
         * SKU库存
         */
        private Integer stock;
        
        /**
         * SKU图片
         */
        private String image;
        
        /**
         * 规格属性
         */
        private List<SpecAttribute> specAttributes;
    }
    
    @Data
    public static class SpecAttribute {
        /**
         * 属性名
         */
        private String name;
        
        /**
         * 属性值
         */
        private String value;
    }
}