package com.ecommerce.crm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户分组实体
 * 
 * 用于客户群组管理：VIP、新客、复购客户等
 */
@Entity
@Table(name = "customer_groups", indexes = {
    @Index(name = "idx_group_name", columnList = "name"),
    @Index(name = "idx_group_code", columnList = "code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;  // 分组名称
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;  // 分组编码（唯一标识）
    
    @Column(length = 200)
    private String description;  // 分组描述
    
    // 分组类型
    @Column(length = 20)
    @Builder.Default
    private String type = "MANUAL";  // MANUAL-手动分组, AUTO-自动分组, SYSTEM-系统分组
    
    // 自动分组规则
    @Column(name = "auto_rule", columnDefinition = "TEXT")
    private String autoRule;  // 自动分组规则（JSON格式）
    // 示例规则: {"conditions": [{"field": "totalAmount", "operator": ">=", "value": 10000}]}
    
    // 分组权益
    @Column(name = "discount_rate", columnDefinition = "DECIMAL(5,4)")
    private java.math.BigDecimal discountRate;  // 折扣率，如0.95表示95折
    
    @Column(name = "point_rate")
    private Integer pointRate;  // 积分倍率
    
    @Column(name = "free_shipping")
    @Builder.Default
    private Boolean freeShipping = false;  // 是否包邮
    
    @Column(name = "exclusive_service")
    @Builder.Default
    private Boolean exclusiveService = false;  // 是否专属客服
    
    // 显示设置
    @Column(length = 20)
    private String color;  // 分组颜色
    
    @Column(length = 50)
    private String icon;  // 分组图标
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;  // 排序
    
    // 状态
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;  // 是否启用
    
    // 统计
    @Column(name = "customer_count")
    @Builder.Default
    private Integer customerCount = 0;  // 分组内客户数
    
    // 创建信息
    @Column(name = "created_by")
    private Long createdBy;  // 创建人ID
    
    @Column(name = "created_by_name", length = 50)
    private String createdByName;  // 创建人名称
    
    // 时间戳
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}