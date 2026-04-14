package com.ecommerce.platform.a1688.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品详情响应DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    
    /**
     * 商品ID
     */
    private String productId;
    
    /**
     * 商品标题
     */
    private String subject;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品主图
     */
    private String mainImage;
    
    /**
     * 商品图片列表
     */
    private List<String> images;
    
    /**
     * 商品视频
     */
    private String videoUrl;
    
    /**
     * 价格区间
     */
    private ProductSearchResponse.PriceRange priceRange;
    
    /**
     * SKU列表
     */
    private List<SkuInfo> skuList;
    
    /**
     * 规格属性列表
     */
    private List<SpecAttribute> specAttributes;
    
    /**
     * 起订量
     */
    private Integer minOrderQuantity;
    
    /**
     * 总库存
     */
    private Integer totalStock;
    
    /**
     * 单位
     */
    private String unit;
    
    /**
     * 类目信息
     */
    private CategoryInfo categoryInfo;
    
    /**
     * 供应商信息
     */
    private ProductSearchResponse.SellerInfo sellerInfo;
    
    /**
     * 发货信息
     */
    private ShippingInfo shippingInfo;
    
    /**
     * 支付信息
     */
    private PaymentInfo paymentInfo;
    
    /**
     * 商品属性
     */
    private List<ProductAttribute> attributes;
    
    /**
     * 商品详情URL
     */
    private String detailUrl;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 错误码
     */
    private Integer errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * SKU信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuInfo {
        
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * SKU编码
         */
        private String skuCode;
        
        /**
         * SKU规格
         */
        private String specName;
        
        /**
         * 规格值
         */
        private List<SpecValue> specValues;
        
        /**
         * 价格（分）
         */
        private Long price;
        
        /**
         * 库存
         */
        private Integer stock;
        
        /**
         * SKU图片
         */
        private String image;
        
        /**
         * 是否可用
         */
        private Boolean available;
    }
    
    /**
     * 规格值
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecValue {
        
        /**
         * 属性名
         */
        private String attrName;
        
        /**
         * 属性值
         */
        private String attrValue;
        
        /**
         * 属性ID
         */
        private String attrId;
        
        /**
         * 属性值ID
         */
        private String valueId;
    }
    
    /**
     * 规格属性
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecAttribute {
        
        /**
         * 属性ID
         */
        private String attrId;
        
        /**
         * 属性名
         */
        private String attrName;
        
        /**
         * 属性值列表
         */
        private List<AttrValue> values;
    }
    
    /**
     * 属性值
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttrValue {
        
        /**
         * 值ID
         */
        private String valueId;
        
        /**
         * 值名称
         */
        private String valueName;
        
        /**
         * 值图片
         */
        private String imageUrl;
    }
    
    /**
     * 类目信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        
        /**
         * 类目ID
         */
        private String categoryId;
        
        /**
         * 类目名称
         */
        private String categoryName;
        
        /**
         * 父类目ID
         */
        private String parentId;
        
        /**
         * 类目路径
         */
        private String categoryPath;
    }
    
    /**
     * 发货信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfo {
        
        /**
         * 发货地
         */
        private String sendAddress;
        
        /**
         * 物流类型
         */
        private String logisticsType;
        
        /**
         * 运费模板
         */
        private String freightTemplate;
        
        /**
         * 发货时效（小时）
         */
        private Integer deliveryTime;
    }
    
    /**
     * 支付信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        
        /**
         * 支持的支付方式
         */
        private List<String> paymentMethods;
        
        /**
         * 是否支持分期
         */
        private Boolean supportInstallment;
        
        /**
         * 是否支持账期
         */
        private Boolean supportCredit;
    }
    
    /**
     * 商品属性
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAttribute {
        
        /**
         * 属性名
         */
        private String attrName;
        
        /**
         * 属性值
         */
        private String attrValue;
    }
}