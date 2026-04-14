package com.ecommerce.crm.entity;

import com.ecommerce.crm.enums.CustomerActivity;
import com.ecommerce.crm.enums.CustomerLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 客户实体
 * 
 * 存储客户基本信息、消费统计、画像数据
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_phone", columnList = "phone"),
    @Index(name = "idx_customer_email", columnList = "email"),
    @Index(name = "idx_customer_level", columnList = "level"),
    @Index(name = "idx_customer_source", columnList = "source"),
    @Index(name = "idx_customer_store", columnList = "storeId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 基本信息
    @Column(length = 50)
    private String name;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 50)
    private String wechat;
    
    @Column(length = 200)
    private String avatar;
    
    @Column(length = 10)
    private String gender;
    
    private LocalDate birthday;
    
    @Column(length = 200)
    private String address;
    
    @Column(length = 50)
    private String province;
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 50)
    private String district;
    
    // 来源信息
    @Column(length = 50)
    private String source;  // 来源渠道：抖音、小红书、1688等
    
    @Column(name = "external_id", length = 100)
    private String externalId;  // 外部平台用户ID
    
    @Column(name = "store_id")
    private Long storeId;  // 店铺ID
    
    // 客户等级
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private CustomerLevel level = CustomerLevel.NORMAL;
    
    // 消费统计
    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;  // 总订单数
    
    @Column(name = "total_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;  // 总消费金额
    
    @Column(name = "avg_order_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal avgOrderAmount = BigDecimal.ZERO;  // 平均客单价
    
    @Column(name = "max_order_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal maxOrderAmount = BigDecimal.ZERO;  // 最大单笔消费
    
    @Column(name = "refund_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;  // 退款金额
    
    @Column(name = "refund_count")
    @Builder.Default
    private Integer refundCount = 0;  // 退款次数
    
    // 活跃度分析
    @Enumerated(EnumType.STRING)
    @Column(name = "activity", length = 20)
    @Builder.Default
    private CustomerActivity activity = CustomerActivity.NORMAL;
    
    @Column(name = "last_order_time")
    private LocalDateTime lastOrderTime;  // 最近下单时间
    
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;  // 最近登录时间
    
    @Column(name = "last_consult_time")
    private LocalDateTime lastConsultTime;  // 最近咨询时间
    
    @Column(name = "login_count")
    @Builder.Default
    private Integer loginCount = 0;  // 登录次数
    
    @Column(name = "consult_count")
    @Builder.Default
    private Integer consultCount = 0;  // 咨询次数
    
    // 画像标签
    @Column(name = "consumption_preference", length = 500)
    private String consumptionPreference;  // 消费偏好（JSON）
    
    @Column(name = "price_sensitivity", length = 20)
    private String priceSensitivity;  // 价格敏感度：高/中/低
    
    @Column(name = "brand_preference", length = 200)
    private String brandPreference;  // 品牌偏好
    
    @Column(columnDefinition = "TEXT")
    private String remark;  // 备注
    
    // 客户标签关联
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "customer_tag_relation",
        joinColumns = @JoinColumn(name = "customer_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<CustomerTag> tags = new HashSet<>();
    
    // 客户分组关联
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private CustomerGroup group;
    
    // 状态
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;  // 是否启用
    
    @Column(name = "is_blacklist")
    @Builder.Default
    private Boolean isBlacklist = false;  // 是否黑名单
    
    // 客服信息
    @Column(name = "service_staff_id")
    private Long serviceStaffId;  // 服务人员ID
    
    @Column(name = "service_staff_name", length = 50)
    private String serviceStaffName;  // 服务人员名称
    
    // 时间戳
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 更新消费统计
     */
    public void updateOrderStats(BigDecimal orderAmount) {
        this.totalOrders++;
        this.totalAmount = this.totalAmount.add(orderAmount);
        this.avgOrderAmount = this.totalAmount.divide(
            BigDecimal.valueOf(this.totalOrders), 2, BigDecimal.ROUND_HALF_UP
        );
        if (orderAmount.compareTo(this.maxOrderAmount) > 0) {
            this.maxOrderAmount = orderAmount;
        }
        this.lastOrderTime = LocalDateTime.now();
        // 自动更新客户等级
        this.level = CustomerLevel.calculateByAmount(this.totalAmount);
    }
    
    /**
     * 记录退款
     */
    public void recordRefund(BigDecimal refundAmount) {
        this.refundCount++;
        this.refundAmount = this.refundAmount.add(refundAmount);
        this.totalAmount = this.totalAmount.subtract(refundAmount);
        if (this.totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.totalAmount = BigDecimal.ZERO;
        }
        // 重新计算等级
        this.level = CustomerLevel.calculateByAmount(this.totalAmount);
    }
    
    /**
     * 获取RFM评分
     */
    public int calculateRfmScore() {
        int rScore = calculateRecencyScore();
        int fScore = calculateFrequencyScore();
        int mScore = calculateMonetaryScore();
        return rScore + fScore + mScore;
    }
    
    private int calculateRecencyScore() {
        if (lastOrderTime == null) return 1;
        long days = java.time.temporal.ChronoUnit.DAYS.between(lastOrderTime, LocalDateTime.now());
        if (days <= 7) return 5;
        if (days <= 30) return 4;
        if (days <= 60) return 3;
        if (days <= 90) return 2;
        return 1;
    }
    
    private int calculateFrequencyScore() {
        if (totalOrders == null || totalOrders == 0) return 1;
        if (totalOrders >= 20) return 5;
        if (totalOrders >= 10) return 4;
        if (totalOrders >= 5) return 3;
        if (totalOrders >= 2) return 2;
        return 1;
    }
    
    private int calculateMonetaryScore() {
        if (totalAmount == null) return 1;
        double amount = totalAmount.doubleValue();
        if (amount >= 10000) return 5;
        if (amount >= 5000) return 4;
        if (amount >= 2000) return 3;
        if (amount >= 500) return 2;
        return 1;
    }
}