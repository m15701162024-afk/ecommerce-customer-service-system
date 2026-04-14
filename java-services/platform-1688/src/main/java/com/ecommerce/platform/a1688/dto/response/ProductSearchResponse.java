package com.ecommerce.platform.a1688.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品搜索响应DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {
    
    /**
     * 商品列表
     */
    private List<ProductItem> products;
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 当前页码
     */
    private Integer pageNo;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
    
    /**
     * 是否有更多
     */
    private Boolean hasMore;
    
    /**
     * 错误码
     */
    private Integer errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 商品项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItem {
        
        /**
         * 商品ID
         */
        private String productId;
        
        /**
         * 商品标题
         */
        private String subject;
        
        /**
         * 商品主图
         */
        private String mainImage;
        
        /**
         * 商品图片列表
         */
        private List<String> images;
        
        /**
         * 价格区间
         */
        private PriceRange priceRange;
        
        /**
         * 起订量
         */
        private Integer minOrderQuantity;
        
        /**
         * 库存
         */
        private Integer stock;
        
        /**
         * 单位
         */
        private String unit;
        
        /**
         * 类目ID
         */
        private String categoryId;
        
        /**
         * 类目名称
         */
        private String categoryName;
        
        /**
         * 供应商信息
         */
        private SellerInfo sellerInfo;
        
        /**
         * 是否支持一件代发
         */
        private Boolean supportDropship;
        
        /**
         * 商品URL
         */
        private String productUrl;
        
        /**
         * 销量
         */
        private Integer salesCount;
        
        /**
         * 评价数
         */
        private Integer reviewCount;
    }
    
    /**
     * 价格区间
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceRange {
        
        /**
         * 最低价格（分）
         */
        private Long minPrice;
        
        /**
         * 最高价格（分）
         */
        private Long maxPrice;
        
        /**
         * 币种
         */
        @Builder.Default
        private String currency = "CNY";
    }
    
    /**
     * 供应商信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerInfo {
        
        /**
         * 会员ID
         */
        private String memberId;
        
        /**
         * 公司名称
         */
        private String companyName;
        
        /**
         * 是否实力商家
         */
        private Boolean isPowerSeller;
        
        /**
         * 信用等级
         */
        private Integer creditLevel;
        
        /**
         * 店铺评分
         */
        private Double shopRating;
        
        /**
         * 经营年限
         */
        private Integer businessYears;
        
        /**
         * 主营类目
         */
        private String mainCategory;
        
        /**
         * 所在地
         */
        private String location;
    }
}