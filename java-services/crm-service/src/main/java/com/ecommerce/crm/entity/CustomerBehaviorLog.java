package com.ecommerce.crm.entity;

import com.ecommerce.crm.enums.BehaviorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户行为日志实体
 * 
 * 记录客户的行为轨迹：浏览、下单、咨询等
 */
@Entity
@Table(name = "customer_behavior_logs", indexes = {
    @Index(name = "idx_behavior_customer", columnList = "customerId"),
    @Index(name = "idx_behavior_type", columnList = "behaviorType"),
    @Index(name = "idx_behavior_time", columnList = "behaviorTime"),
    @Index(name = "idx_behavior_order", columnList = "orderId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBehaviorLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 客户信息
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "customer_name", length = 50)
    private String customerName;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    // 行为类型
    @Enumerated(EnumType.STRING)
    @Column(name = "behavior_type", nullable = false, length = 30)
    private BehaviorType behaviorType;
    
    // 行为详情
    @Column(name = "behavior_desc", length = 500)
    private String behaviorDesc;  // 行为描述
    
    @Column(name = "behavior_time", nullable = false)
    private LocalDateTime behaviorTime;  // 行为发生时间
    
    // 关联对象
    @Column(name = "target_id")
    private Long targetId;  // 关联对象ID（商品ID、订单ID等）
    
    @Column(name = "target_type", length = 30)
    private String targetType;  // 关联对象类型：PRODUCT、ORDER、CONSULTATION等
    
    @Column(name = "target_name", length = 200)
    private String targetName;  // 关联对象名称
    
    // 订单相关
    @Column(name = "order_id")
    private Long orderId;  // 订单ID
    
    @Column(name = "order_no", length = 50)
    private String orderNo;  // 订单编号
    
    @Column(name = "order_amount", precision = 12, scale = 2)
    private BigDecimal orderAmount;  // 订单金额
    
    // 商品相关
    @Column(name = "product_id")
    private Long productId;  // 商品ID
    
    @Column(name = "product_name", length = 200)
    private String productName;  // 商品名称
    
    @Column(name = "product_sku", length = 50)
    private String productSku;  // 商品SKU
    
    @Column(name = "product_price", precision = 12, scale = 2)
    private BigDecimal productPrice;  // 商品价格
    
    // 来源渠道
    @Column(length = 50)
    private String channel;  // 渠道：APP、H5、小程序、PC等
    
    @Column(length = 50)
    private String platform;  // 平台：抖音、小红书、1688等
    
    // 咨询相关
    @Column(name = "consult_session_id")
    private Long consultSessionId;  // 咨询会话ID
    
    @Column(name = "service_staff_id")
    private Long serviceStaffId;  // 服务人员ID
    
    @Column(name = "service_staff_name", length = 50)
    private String serviceStaffName;  // 服务人员名称
    
    // 评价相关
    @Column(name = "rating")
    private Integer rating;  // 评分（1-5）
    
    @Column(name = "review_content", columnDefinition = "TEXT")
    private String reviewContent;  // 评价内容
    
    // 设备信息
    @Column(length = 500)
    private String userAgent;  // 用户代理
    
    @Column(length = 50)
    private String ip;  // IP地址
    
    @Column(length = 50)
    private String device;  // 设备类型：iOS、Android、PC等
    
    // 扩展数据
    @Column(columnDefinition = "TEXT")
    private String extra;  // 扩展数据（JSON格式）
    
    // 时间戳
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 创建浏览行为日志
     */
    public static CustomerBehaviorLog createViewLog(Long customerId, Long productId, String productName) {
        return CustomerBehaviorLog.builder()
            .customerId(customerId)
            .behaviorType(BehaviorType.VIEW_PRODUCT)
            .behaviorTime(LocalDateTime.now())
            .productId(productId)
            .productName(productName)
            .targetId(productId)
            .targetType("PRODUCT")
            .build();
    }
    
    /**
     * 创建下单行为日志
     */
    public static CustomerBehaviorLog createOrderLog(Long customerId, Long orderId, String orderNo, BigDecimal amount) {
        return CustomerBehaviorLog.builder()
            .customerId(customerId)
            .behaviorType(BehaviorType.PLACE_ORDER)
            .behaviorTime(LocalDateTime.now())
            .orderId(orderId)
            .orderNo(orderNo)
            .orderAmount(amount)
            .targetId(orderId)
            .targetType("ORDER")
            .build();
    }
    
    /**
     * 创建支付行为日志
     */
    public static CustomerBehaviorLog createPaymentLog(Long customerId, Long orderId, String orderNo, BigDecimal amount) {
        return CustomerBehaviorLog.builder()
            .customerId(customerId)
            .behaviorType(BehaviorType.PAYMENT)
            .behaviorTime(LocalDateTime.now())
            .orderId(orderId)
            .orderNo(orderNo)
            .orderAmount(amount)
            .targetId(orderId)
            .targetType("ORDER")
            .build();
    }
    
    /**
     * 创建咨询行为日志
     */
    public static CustomerBehaviorLog createConsultLog(Long customerId, Long sessionId, Long staffId, String staffName) {
        return CustomerBehaviorLog.builder()
            .customerId(customerId)
            .behaviorType(BehaviorType.CONSULTATION)
            .behaviorTime(LocalDateTime.now())
            .consultSessionId(sessionId)
            .serviceStaffId(staffId)
            .serviceStaffName(staffName)
            .targetId(sessionId)
            .targetType("CONSULTATION")
            .build();
    }
}