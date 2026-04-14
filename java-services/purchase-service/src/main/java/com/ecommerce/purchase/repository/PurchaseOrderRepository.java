package com.ecommerce.purchase.repository;

import com.ecommerce.purchase.entity.PurchaseOrder;
import com.ecommerce.purchase.entity.PurchaseOrder.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采购订单仓库接口
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * 按创建时间倒序查询所有订单
     */
    List<PurchaseOrder> findAllByOrderByCreateTimeDesc();

    /**
     * 按状态查询订单
     */
    List<PurchaseOrder> findByStatus(OrderStatus status);

    /**
     * 按供应商查询订单
     */
    List<PurchaseOrder> findBySupplierName(String supplierName);

    /**
     * 按采购员查询订单
     */
    List<PurchaseOrder> findByPurchaser(String purchaser);

    /**
     * 按多个状态查询订单
     */
    List<PurchaseOrder> findByStatusIn(List<OrderStatus> statuses);
}