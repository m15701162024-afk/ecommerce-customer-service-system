package com.ecommerce.crm.repository;

import com.ecommerce.crm.entity.CustomerBehaviorLog;
import com.ecommerce.crm.enums.BehaviorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户行为日志Repository
 */
@Repository
public interface CustomerBehaviorLogRepository extends JpaRepository<CustomerBehaviorLog, Long> {
    
    // 按客户查询
    List<CustomerBehaviorLog> findByCustomerId(Long customerId);
    
    Page<CustomerBehaviorLog> findByCustomerId(Long customerId, Pageable pageable);
    
    List<CustomerBehaviorLog> findByCustomerIdOrderByBehaviorTimeDesc(Long customerId);
    
    // 按行为类型查询
    List<CustomerBehaviorLog> findByBehaviorType(BehaviorType behaviorType);
    
    Page<CustomerBehaviorLog> findByBehaviorType(BehaviorType behaviorType, Pageable pageable);
    
    // 按客户和行为类型查询
    List<CustomerBehaviorLog> findByCustomerIdAndBehaviorType(Long customerId, BehaviorType behaviorType);
    
    Page<CustomerBehaviorLog> findByCustomerIdAndBehaviorType(Long customerId, BehaviorType behaviorType, Pageable pageable);
    
    // 按时间范围查询
    List<CustomerBehaviorLog> findByBehaviorTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    Page<CustomerBehaviorLog> findByBehaviorTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    @Query("SELECT l FROM CustomerBehaviorLog l WHERE l.customerId = :customerId AND l.behaviorTime BETWEEN :startTime AND :endTime ORDER BY l.behaviorTime DESC")
    List<CustomerBehaviorLog> findByCustomerIdAndTimeRange(
        @Param("customerId") Long customerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    // 按订单查询
    List<CustomerBehaviorLog> findByOrderId(Long orderId);
    
    Optional<CustomerBehaviorLog> findFirstByOrderIdOrderByBehaviorTimeDesc(Long orderId);
    
    // 按商品查询
    List<CustomerBehaviorLog> findByProductId(Long productId);
    
    Page<CustomerBehaviorLog> findByProductId(Long productId, Pageable pageable);
    
    // 按咨询会话查询
    List<CustomerBehaviorLog> findByConsultSessionId(Long consultSessionId);
    
    // 多条件搜索
    @Query("SELECT l FROM CustomerBehaviorLog l WHERE " +
           "(:customerId IS NULL OR l.customerId = :customerId) AND " +
           "(:behaviorType IS NULL OR l.behaviorType = :behaviorType) AND " +
           "(:channel IS NULL OR l.channel = :channel) AND " +
           "(:platform IS NULL OR l.platform = :platform) AND " +
           "(:startTime IS NULL OR l.behaviorTime >= :startTime) AND " +
           "(:endTime IS NULL OR l.behaviorTime <= :endTime)")
    Page<CustomerBehaviorLog> searchByConditions(
        @Param("customerId") Long customerId,
        @Param("behaviorType") BehaviorType behaviorType,
        @Param("channel") String channel,
        @Param("platform") String platform,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );
    
    // 统计查询
    @Query("SELECT COUNT(l) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(l) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId AND l.behaviorType = :behaviorType")
    Long countByCustomerIdAndBehaviorType(@Param("customerId") Long customerId, @Param("behaviorType") BehaviorType behaviorType);
    
    @Query("SELECT COUNT(l) FROM CustomerBehaviorLog l WHERE l.behaviorType = :behaviorType AND l.behaviorTime BETWEEN :startTime AND :endTime")
    Long countByBehaviorTypeAndTimeRange(
        @Param("behaviorType") BehaviorType behaviorType,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    // 金额统计
    @Query("SELECT SUM(l.orderAmount) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId AND l.behaviorType = 'PAYMENT'")
    BigDecimal sumPaymentAmountByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(l.orderAmount) FROM CustomerBehaviorLog l WHERE l.behaviorType = 'PAYMENT' AND l.behaviorTime BETWEEN :startTime AND :endTime")
    BigDecimal sumPaymentAmountByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 行为类型分组统计
    @Query("SELECT l.behaviorType, COUNT(l) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId GROUP BY l.behaviorType")
    List<Object[]> countGroupByBehaviorType(@Param("customerId") Long customerId);
    
    @Query("SELECT l.behaviorType, COUNT(l) FROM CustomerBehaviorLog l WHERE l.behaviorTime BETWEEN :startTime AND :endTime GROUP BY l.behaviorType")
    List<Object[]> countGroupByBehaviorTypeAndTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 渠道统计
    @Query("SELECT l.channel, COUNT(l) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId GROUP BY l.channel")
    List<Object[]> countGroupByChannel(@Param("customerId") Long customerId);
    
    // 平台统计
    @Query("SELECT l.platform, COUNT(l) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId GROUP BY l.platform")
    List<Object[]> countGroupByPlatform(@Param("customerId") Long customerId);
    
    // 最近行为
    @Query("SELECT l FROM CustomerBehaviorLog l WHERE l.customerId = :customerId ORDER BY l.behaviorTime DESC LIMIT :limit")
    List<CustomerBehaviorLog> findRecentBehaviors(@Param("customerId") Long customerId, @Param("limit") int limit);
    
    // 获取客户最后下单时间
    @Query("SELECT MAX(l.behaviorTime) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId AND l.behaviorType = 'PLACE_ORDER'")
    LocalDateTime findLastOrderTime(@Param("customerId") Long customerId);
    
    // 获取客户最后登录时间
    @Query("SELECT MAX(l.behaviorTime) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId AND l.behaviorType = 'LOGIN'")
    LocalDateTime findLastLoginTime(@Param("customerId") Long customerId);
    
    // 获取客户最后咨询时间
    @Query("SELECT MAX(l.behaviorTime) FROM CustomerBehaviorLog l WHERE l.customerId = :customerId AND l.behaviorType = 'CONSULTATION'")
    LocalDateTime findLastConsultTime(@Param("customerId") Long customerId);
    
    // 服务人员相关
    List<CustomerBehaviorLog> findByServiceStaffId(Long serviceStaffId);
    
    Page<CustomerBehaviorLog> findByServiceStaffId(Long serviceStaffId, Pageable pageable);
    
    @Query("SELECT COUNT(l) FROM CustomerBehaviorLog l WHERE l.serviceStaffId = :staffId AND l.behaviorType = 'CONSULTATION'")
    Long countConsultationsByServiceStaff(@Param("staffId") Long staffId);
}