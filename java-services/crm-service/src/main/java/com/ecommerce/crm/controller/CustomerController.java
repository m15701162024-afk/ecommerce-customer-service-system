package com.ecommerce.crm.controller;

import com.ecommerce.crm.dto.*;
import com.ecommerce.crm.enums.CustomerActivity;
import com.ecommerce.crm.enums.CustomerLevel;
import com.ecommerce.crm.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 客户控制器
 * 
 * 提供客户管理相关的REST API
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    
    private final CustomerService customerService;
    
    // ==================== 客户CRUD ====================
    
    /**
     * 创建客户
     * POST /api/v1/customers
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('crm:manage')")
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        log.info("Creating customer: {}", request.getName());
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 更新客户
     * PUT /api/v1/customers/{id}
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('crm:manage')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id, 
            @Valid @RequestBody CustomerUpdateRequest request) {
        log.info("Updating customer: {}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取客户详情
     * GET /api/v1/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomer(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除客户
     * DELETE /api/v1/customers/{id}
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('crm:manage')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(Map.of("message", "客户删除成功"));
    }
    
    // ==================== 客户搜索 ====================
    
    /**
     * 搜索客户
     * GET /api/v1/customers/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponse>> searchCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Boolean isBlacklist,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        CustomerLevel levelEnum = level != null ? CustomerLevel.fromCode(level) : null;
        CustomerActivity activityEnum = activity != null ? CustomerActivity.fromCode(activity) : null;
        
        Page<CustomerResponse> result = customerService.searchCustomers(
            keyword, levelEnum, activityEnum, source, storeId, isBlacklist, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取客户列表
     * GET /api/v1/customers
     */
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerResponse> result = customerService.searchCustomers(
            null, null, null, null, null, null, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    // ==================== 按条件查询 ====================
    
    /**
     * 按等级获取客户
     * GET /api/v1/customers/level/{level}
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<Page<CustomerResponse>> getCustomersByLevel(
            @PathVariable String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        CustomerLevel levelEnum = CustomerLevel.fromCode(level);
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalAmount").descending());
        
        Page<CustomerResponse> result = customerService.getCustomersByLevel(levelEnum, pageable);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 按活跃度获取客户
     * GET /api/v1/customers/activity/{activity}
     */
    @GetMapping("/activity/{activity}")
    public ResponseEntity<Page<CustomerResponse>> getCustomersByActivity(
            @PathVariable String activity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        CustomerActivity activityEnum = CustomerActivity.fromCode(activity);
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastLoginTime").descending());
        
        Page<CustomerResponse> result = customerService.getCustomersByActivity(activityEnum, pageable);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取高价值客户
     * GET /api/v1/customers/high-value
     */
    @GetMapping("/high-value")
    public ResponseEntity<List<CustomerResponse>> getHighValueCustomers(
            @RequestParam(defaultValue = "10000") BigDecimal minAmount,
            @RequestParam(defaultValue = "100") int limit) {
        
        List<CustomerResponse> result = customerService.getHighValueCustomers(minAmount, limit);
        return ResponseEntity.ok(result);
    }
    
    // ==================== 标签管理 ====================
    
    /**
     * 添加标签
     * POST /api/v1/customers/{id}/tags
     */
    @PostMapping("/{id}/tags")
    public ResponseEntity<Map<String, String>> addTags(
            @PathVariable Long id,
            @RequestBody List<Long> tagIds) {
        log.info("Adding tags to customer {}: {}", id, tagIds);
        customerService.addTags(id, tagIds);
        return ResponseEntity.ok(Map.of("message", "标签添加成功"));
    }
    
    /**
     * 移除标签
     * DELETE /api/v1/customers/{id}/tags
     */
    @DeleteMapping("/{id}/tags")
    public ResponseEntity<Map<String, String>> removeTags(
            @PathVariable Long id,
            @RequestBody List<Long> tagIds) {
        log.info("Removing tags from customer {}: {}", id, tagIds);
        customerService.removeTags(id, tagIds);
        return ResponseEntity.ok(Map.of("message", "标签移除成功"));
    }
    
    /**
     * 批量添加标签
     * POST /api/v1/customers/tags/batch-add
     */
    @PostMapping("/tags/batch-add")
    public ResponseEntity<Map<String, String>> batchAddTags(@Valid @RequestBody CustomerTagOperationRequest request) {
        log.info("Batch adding tags to {} customers", request.getCustomerIds().size());
        customerService.batchAddTags(request);
        return ResponseEntity.ok(Map.of("message", "批量添加标签成功"));
    }
    
    /**
     * 批量移除标签
     * POST /api/v1/customers/tags/batch-remove
     */
    @PostMapping("/tags/batch-remove")
    public ResponseEntity<Map<String, String>> batchRemoveTags(@Valid @RequestBody CustomerTagOperationRequest request) {
        log.info("Batch removing tags from {} customers", request.getCustomerIds().size());
        customerService.batchRemoveTags(request);
        return ResponseEntity.ok(Map.of("message", "批量移除标签成功"));
    }
    
    // ==================== 分组管理 ====================
    
    /**
     * 设置客户分组
     * PUT /api/v1/customers/{id}/group
     */
    @PutMapping("/{id}/group")
    public ResponseEntity<Map<String, String>> setGroup(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        Long groupId = request.get("groupId");
        log.info("Setting group for customer {}: {}", id, groupId);
        customerService.setGroup(id, groupId);
        return ResponseEntity.ok(Map.of("message", "分组设置成功"));
    }
    
    // ==================== 等级管理 ====================
    
    /**
     * 更新客户等级
     * PUT /api/v1/customers/{id}/level
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('crm:manage')")
    @PutMapping("/{id}/level")
    public ResponseEntity<Map<String, String>> updateLevel(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String level = request.get("level");
        log.info("Updating level for customer {}: {}", id, level);
        customerService.updateLevel(id, CustomerLevel.fromCode(level));
        return ResponseEntity.ok(Map.of("message", "等级更新成功"));
    }
    
    // ==================== 黑名单管理 ====================
    
    /**
     * 加入黑名单
     * POST /api/v1/customers/{id}/blacklist
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('crm:blacklist')")
    @PostMapping("/{id}/blacklist")
    public ResponseEntity<Map<String, String>> addToBlacklist(@PathVariable Long id) {
        log.info("Adding customer {} to blacklist", id);
        customerService.addToBlacklist(id);
        return ResponseEntity.ok(Map.of("message", "已加入黑名单"));
    }
    
    /**
     * 移出黑名单
     * DELETE /api/v1/customers/{id}/blacklist
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('crm:blacklist')")
    @DeleteMapping("/{id}/blacklist")
    public ResponseEntity<Map<String, String>> removeFromBlacklist(@PathVariable Long id) {
        log.info("Removing customer {} from blacklist", id);
        customerService.removeFromBlacklist(id);
        return ResponseEntity.ok(Map.of("message", "已移出黑名单"));
    }
    
    // ==================== 统计分析 ====================
    
    /**
     * 获取客户统计
     * GET /api/v1/customers/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<CustomerStatisticsResponse> getStatistics() {
        CustomerStatisticsResponse statistics = customerService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    // ==================== 健康检查 ====================
    
    /**
     * 健康检查
     * GET /api/v1/customers/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "crm-service"
        ));
    }
}