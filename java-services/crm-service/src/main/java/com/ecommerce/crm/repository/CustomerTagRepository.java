package com.ecommerce.crm.repository;

import com.ecommerce.crm.entity.CustomerTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 客户标签Repository
 */
@Repository
public interface CustomerTagRepository extends JpaRepository<CustomerTag, Long> {
    
    // 基本查询
    Optional<CustomerTag> findByCode(String code);
    
    Optional<CustomerTag> findByName(String name);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
    
    // 按分类查询
    List<CustomerTag> findByCategory(String category);
    
    Page<CustomerTag> findByCategory(String category, Pageable pageable);
    
    // 按类型查询
    List<CustomerTag> findByType(String type);
    
    Page<CustomerTag> findByType(String type, Pageable pageable);
    
    // 启用状态查询
    List<CustomerTag> findByEnabledTrue();
    
    List<CustomerTag> findByEnabledTrueOrderBySortOrderAsc();
    
    Page<CustomerTag> findByEnabledTrue(Pageable pageable);
    
    // 多条件搜索
    @Query("SELECT t FROM CustomerTag t WHERE " +
           "(:name IS NULL OR t.name LIKE %:name%) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:enabled IS NULL OR t.enabled = :enabled)")
    Page<CustomerTag> searchByConditions(
        @Param("name") String name,
        @Param("category") String category,
        @Param("type") String type,
        @Param("enabled") Boolean enabled,
        Pageable pageable
    );
    
    // 按编码集合查询
    List<CustomerTag> findByCodeIn(Set<String> codes);
    
    List<CustomerTag> findByIdIn(Set<Long> ids);
    
    // 统计查询
    @Query("SELECT COUNT(t) FROM CustomerTag t WHERE t.enabled = true")
    Long countEnabled();
    
    @Query("SELECT t.category, COUNT(t) FROM CustomerTag t GROUP BY t.category")
    List<Object[]> countGroupByCategory();
    
    // 更新客户计数
    @Modifying
    @Query("UPDATE CustomerTag t SET t.customerCount = (SELECT COUNT(c) FROM Customer c JOIN c.tags t WHERE t.id = :tagId) WHERE t.id = :tagId")
    int updateCustomerCount(@Param("tagId") Long tagId);
    
    // 批量更新客户计数
    @Modifying
    @Query("UPDATE CustomerTag t SET t.customerCount = (SELECT COUNT(c) FROM Customer c JOIN c.tags t WHERE t.id = t.id)")
    void updateAllCustomerCounts();
    
    // 按创建人查询
    List<CustomerTag> findByCreatedBy(Long createdBy);
    
    Page<CustomerTag> findByCreatedBy(Long createdBy, Pageable pageable);
    
    // 获取所有分类
    @Query("SELECT DISTINCT t.category FROM CustomerTag t WHERE t.category IS NOT NULL")
    List<String> findAllCategories();
    
    // 排序查询
    List<CustomerTag> findAllByOrderBySortOrderAsc();
    
    List<CustomerTag> findByEnabledTrueOrderBySortOrderAscNameAsc();
}