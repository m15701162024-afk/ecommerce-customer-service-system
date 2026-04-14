package com.ecommerce.purchase.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单实体
 */
@Data
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 采购订单号
     */
    @Column(name = "order_no", unique = true, nullable = false, length = 32)
    private String orderNo;

    /**
     * 供应商名称
     */
    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    /**
     * 商品名称
     */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /**
     * 商品ID (用于库存扣减)
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 商品数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 单价
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * 总金额
     */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 订单状态: PENDING-待处理, AUTO_PAID-自动支付, CONFIRMING-待确认, WARNING-预警
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /**
     * 采购员
     */
    @Column(name = "purchaser", length = 50)
    private String purchaser;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 支付时间
     */
    @Column(name = "pay_time")
    private LocalDateTime payTime;

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING("待处理"),
        AUTO_PAID("自动支付"),
        CONFIRMING("待确认"),
        WARNING("预警待确认"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        REJECTED("已拒绝");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}