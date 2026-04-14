package com.ecommerce.crm.controller;

import com.ecommerce.crm.entity.CustomerGroup;
import com.ecommerce.crm.service.CustomerGroupService;
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

import java.util.List;
import java.util.Map;

/**
 * 客户分组控制器
 */
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
public class CustomerGroupController {
    
    private final CustomerGroupService groupService;
    
    /**
     * 创建分组
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CRM_MANAGER')")
    @PostMapping
    public ResponseEntity<CustomerGroup> createGroup(@RequestBody CustomerGroup group) {
        log.info("Creating group: {}", group.getName());
        CustomerGroup created = groupService.createGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * 更新分组
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CRM_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerGroup> updateGroup(@PathVariable Long id, @RequestBody CustomerGroup group) {
        log.info("Updating group: {}", id);
        CustomerGroup updated = groupService.updateGroup(id, group);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 获取分组详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerGroup> getGroup(@PathVariable Long id) {
        return groupService.getGroup(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取所有启用的分组
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<CustomerGroup>> getAllEnabledGroups() {
        List<CustomerGroup> groups = groupService.getAllEnabledGroups();
        return ResponseEntity.ok(groups);
    }
    
    /**
     * 获取系统分组
     */
    @GetMapping("/system")
    public ResponseEntity<List<CustomerGroup>> getSystemGroups() {
        List<CustomerGroup> groups = groupService.getSystemGroups();
        return ResponseEntity.ok(groups);
    }
    
    /**
     * 获取自动分组
     */
    @GetMapping("/auto")
    public ResponseEntity<List<CustomerGroup>> getAutoGroups() {
        List<CustomerGroup> groups = groupService.getAutoGroups();
        return ResponseEntity.ok(groups);
    }
    
    /**
     * 搜索分组
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerGroup>> searchGroups(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<CustomerGroup> result = groupService.searchGroups(name, type, enabled, pageable);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 删除分组
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CRM_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGroup(@PathVariable Long id) {
        log.info("Deleting group: {}", id);
        groupService.deleteGroup(id);
        return ResponseEntity.ok(Map.of("message", "分组删除成功"));
    }
}