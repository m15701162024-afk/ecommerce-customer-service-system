package com.ecommerce.platform.a1688.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单查询请求DTO
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderQueryRequest {
    
    /**
     * 订单状态
     * wait_pay: 待付款
     * wait_send: 待发货
     * wait_receive: 待收货
     * finished: 已完成
     * cancelled: 已取消
     */
    private String orderStatus;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 供应商会员ID
     */
    private String sellerMemberId;
    
    /**
     * 订单号
     */
    private String orderId;
    
    /**
     * 商品名称（模糊搜索）
     */
    private String productName;
    
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
     * 排序字段
     * create_time: 创建时间
     * update_time: 更新时间
     * total_amount: 订单金额
     */
    @Builder.Default
    private String sortField = "create_time";
    
    /**
     * 排序方式
     * asc: 升序
     * desc: 降序
     */
    @Builder.Default
    private String sortOrder = "desc";
}