package com.ecommerce.crm.repository;

import com.ecommerce.crm.entity.Customer;
import com.ecommerce.crm.enums.CustomerActivity;
import com.ecommerce.crm.enums.CustomerLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 客户Repository
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    
    // 基本查询
    Optional<Customer> findByPhone(String phone);
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByWechat(String wechat);
    
    Optional<Customer> findByExternalId(String externalId);
    
    boolean existsByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    // 按等级查询
    List<Customer> findByLevel(CustomerLevel level);
    
    Page<Customer> findByLevel(CustomerLevel level, Pageable pageable);
    
    // 按活跃度查询
    List<Customer> findByActivity(CustomerActivity activity);
    
    Page<Customer> findByActivity(CustomerActivity activity, Pageable pageable);
    
    // 按来源查询
    List<Customer> findBySource(String source);
    
    Page<Customer> findBySource(String source, Pageable pageable);
    
    // 按店铺查询
    List<Customer> findByStoreId(Long storeId);
    
    Page<Customer> findByStoreId(Long storeId, Pageable pageable);
    
    // 按分组查询
    Page<Customer> findByGroupId(Long groupId, Pageable pageable);
    
    // 黑名单查询
    List<Customer> findByIsBlacklistTrue();
    
    Page<Customer> findByIsBlacklistTrue(Pageable pageable);
    
    // 服务人员查询
    List<Customer> findByServiceStaffId(Long serviceStaffId);
    
    Page<Customer> findByServiceStaffId(Long serviceStaffId, Pageable pageable);
    
    // 模糊搜索
    @Query("SELECT c FROM Customer c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.phone LIKE %:keyword% OR c.email LIKE %:keyword%)")
    Page<Customer> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 多条件搜索
    @Query("SELECT c FROM Customer c WHERE " +
           "(:name IS NULL OR c.name LIKE %:name%) AND " +
           "(:phone IS NULL OR c.phone LIKE %:phone%) AND " +
           "(:level IS NULL OR c.level = :level) AND " +
           "(:activity IS NULL OR c.activity = :activity) AND " +
           "(:source IS NULL OR c.source = :source) AND " +
           "(:storeId IS NULL OR c.storeId = :storeId) AND " +
           "(:isBlacklist IS NULL OR c.isBlacklist = :isBlacklist)")
    Page<Customer> searchByConditions(
        @Param("name") String name,
        @Param("phone") String phone,
        @Param("level") CustomerLevel level,
        @Param("activity") CustomerActivity activity,
        @Param("source") String source,
        @Param("storeId") Long storeId,
        @Param("isBlacklist") Boolean isBlacklist,
        Pageable pageable
    );
    
    // 统计查询
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.level = :level")
    Long countByLevel(@Param("level") CustomerLevel level);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.activity = :activity")
    Long countByActivity(@Param("activity") CustomerActivity activity);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.source = :source")
    Long countBySource(@Param("source") String source);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :startTime AND :endTime")
    Long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 消费金额统计
    @Query("SELECT SUM(c.totalAmount) FROM Customer c")
    BigDecimal sumTotalAmount();
    
    @Query("SELECT AVG(c.totalAmount) FROM Customer c WHERE c.totalOrders > 0")
    BigDecimal avgTotalAmount();
    
    // 活跃客户查询
    @Query("SELECT c FROM Customer c WHERE c.lastOrderTime >= :since")
    List<Customer> findActiveCustomersSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT c FROM Customer c WHERE c.lastLoginTime >= :since")
    List<Customer> findRecentlyLoggedIn(@Param("since") LocalDateTime since);
    
    // 流失客户查询
    @Query("SELECT c FROM Customer c WHERE c.lastOrderTime < :before OR c.lastOrderTime IS NULL")
    List<Customer> findInactiveCustomersBefore(@Param("before") LocalDateTime before);
    
    // 高价值客户
    @Query("SELECT c FROM Customer c WHERE c.totalAmount >= :minAmount ORDER BY c.totalAmount DESC")
    List<Customer> findHighValueCustomers(@Param("minAmount") BigDecimal minAmount, Pageable pageable);
    
    // 更新操作
    @Modifying
    @Query("UPDATE Customer c SET c.level = :level WHERE c.id = :id")
    int updateLevel(@Param("id") Long id, @Param("level") CustomerLevel level);
    
    @Modifying
    @Query("UPDATE Customer c SET c.activity = :activity WHERE c.id = :id")
    int updateActivity(@Param("id") Long id, @Param("activity") CustomerActivity activity);
    
    @Modifying
    @Query("UPDATE Customer c SET c.isBlacklist = :isBlacklist WHERE c.id = :id")
    int updateBlacklistStatus(@Param("id") Long id, @Param("isBlacklist") Boolean isBlacklist);
    
    @Modifying
    @Query("UPDATE Customer c SET c.serviceStaffId = :staffId, c.serviceStaffName = :staffName WHERE c.id = :id")
    int updateServiceStaff(@Param("id") Long id, @Param("staffId") Long staffId, @Param("staffName") String staffName);
    
    // 分组统计
    @Query("SELECT c.level, COUNT(c) FROM Customer c GROUP BY c.level")
    List<Object[]> countGroupByLevel();
    
    @Query("SELECT c.activity, COUNT(c) FROM Customer c GROUP BY c.activity")
    List<Object[]> countGroupByActivity();
    
    @Query("SELECT c.source, COUNT(c) FROM Customer c GROUP BY c.source")
    List<Object[]> countGroupBySource();
}