package com.ecommerce.platform.a1688.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单列表响应DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {
    
    /**
     * 订单列表
     */
    private List<OrderDetailResponse> orders;
    
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
}