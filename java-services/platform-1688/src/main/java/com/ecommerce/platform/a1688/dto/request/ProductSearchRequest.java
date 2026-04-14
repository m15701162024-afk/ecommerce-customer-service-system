package com.ecommerce.platform.a1688.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品搜索请求DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    
    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
    
    /**
     * 类目ID
     */
    private String categoryId;
    
    /**
     * 起始价格（分）
     */
    @Min(value = 0, message = "价格不能为负数")
    private Long startPrice;
    
    /**
     * 结束价格（分）
     */
    @Min(value = 0, message = "价格不能为负数")
    private Long endPrice;
    
    /**
     * 起订量
     */
    @Min(value = 1, message = "起订量最小为1")
    private Integer minOrderQuantity;
    
    /**
     * 发货地
     */
    private String sendGoodsAddress;
    
    /**
     * 是否支持一件代发
     */
    private Boolean supportDropship;
    
    /**
     * 是否支持深度验厂
     */
    private Boolean supportDeepInspect;
    
    /**
     * 排序方式
     * price_asc: 价格升序
     * price_desc: 价格降序
     * sales_desc: 销量降序
     * credit_desc: 信用降序
     */
    private String sortType;
    
    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码最小为1")
    @Builder.Default
    private Integer pageNo = 1;
    
    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Builder.Default
    private Integer pageSize = 20;
    
    /**
     * 供应商会员ID列表（用于筛选指定供应商）
     */
    private List<String> sellerMemberIds;
    
    /**
     * 是否只看实力商家
     */
    private Boolean onlyPowerSeller;
    
    /**
     * 商品状态
     * normal: 正常
     * deleted: 已删除
     */
    private String status;
}