package com.ecommerce.crm.repository;

import com.ecommerce.crm.entity.CustomerGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户分组Repository
 */
@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
    
    // 基本查询
    Optional<CustomerGroup> findByCode(String code);
    
    Optional<CustomerGroup> findByName(String name);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
    
    // 按类型查询
    List<CustomerGroup> findByType(String type);
    
    Page<CustomerGroup> findByType(String type, Pageable pageable);
    
    // 启用状态查询
    List<CustomerGroup> findByEnabledTrue();
    
    List<CustomerGroup> findByEnabledTrueOrderBySortOrderAsc();
    
    Page<CustomerGroup> findByEnabledTrue(Pageable pageable);
    
    // 多条件搜索
    @Query("SELECT g FROM CustomerGroup g WHERE " +
           "(:name IS NULL OR g.name LIKE %:name%) AND " +
           "(:type IS NULL OR g.type = :type) AND " +
           "(:enabled IS NULL OR g.enabled = :enabled)")
    Page<CustomerGroup> searchByConditions(
        @Param("name") String name,
        @Param("type") String type,
        @Param("enabled") Boolean enabled,
        Pageable pageable
    );
    
    // 统计查询
    @Query("SELECT COUNT(g) FROM CustomerGroup g WHERE g.enabled = true")
    Long countEnabled();
    
    // 更新客户计数
    @Modifying
    @Query("UPDATE CustomerGroup g SET g.customerCount = (SELECT COUNT(c) FROM Customer c WHERE c.group.id = :groupId) WHERE g.id = :groupId")
    int updateCustomerCount(@Param("groupId") Long groupId);
    
    // 按创建人查询
    List<CustomerGroup> findByCreatedBy(Long createdBy);
    
    Page<CustomerGroup> findByCreatedBy(Long createdBy, Pageable pageable);
    
    // 排序查询
    List<CustomerGroup> findAllByOrderBySortOrderAsc();
    
    List<CustomerGroup> findByEnabledTrueOrderBySortOrderAscNameAsc();
    
    // 获取系统分组
    @Query("SELECT g FROM CustomerGroup g WHERE g.type = 'SYSTEM' AND g.enabled = true")
    List<CustomerGroup> findSystemGroups();
    
    // 获取自动分组
    @Query("SELECT g FROM CustomerGroup g WHERE g.type = 'AUTO' AND g.enabled = true")
    List<CustomerGroup> findAutoGroups();
}