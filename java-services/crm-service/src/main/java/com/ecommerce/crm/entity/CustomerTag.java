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
 * 客户标签实体
 * 
 * 用于客户分类和标记
 */
@Entity
@Table(name = "customer_tags", indexes = {
    @Index(name = "idx_tag_name", columnList = "name"),
    @Index(name = "idx_tag_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTag implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;  // 标签名称
    
    @Column(length = 50)
    private String code;  // 标签编码（唯一标识）
    
    @Column(length = 200)
    private String description;  // 标签描述
    
    @Column(length = 50)
    private String category;  // 标签分类：消费、行为、偏好、属性等
    
    @Column(length = 20)
    private String color;  // 标签颜色（前端展示用）
    
    @Column(length = 50)
    private String icon;  // 标签图标
    
    // 标签类型
    @Column(length = 20)
    @Builder.Default
    private String type = "MANUAL";  // MANUAL-手动标签, AUTO-自动标签, SYSTEM-系统标签
    
    // 自动标签规则
    @Column(name = "auto_rule", columnDefinition = "TEXT")
    private String autoRule;  // 自动标签规则（JSON格式）
    
    // 排序和状态
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;  // 排序
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;  // 是否启用
    
    // 统计
    @Column(name = "customer_count")
    @Builder.Default
    private Integer customerCount = 0;  // 关联客户数
    
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