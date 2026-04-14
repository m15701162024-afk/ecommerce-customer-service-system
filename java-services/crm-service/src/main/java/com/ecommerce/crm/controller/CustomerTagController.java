package com.ecommerce.crm.controller;

import com.ecommerce.crm.entity.CustomerTag;
import com.ecommerce.crm.service.CustomerTagService;
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
 * 客户标签控制器
 */
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Slf4j
public class CustomerTagController {
    
    private final CustomerTagService tagService;
    
    /**
     * 创建标签
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CRM_MANAGER')")
    @PostMapping
    public ResponseEntity<CustomerTag> createTag(@RequestBody CustomerTag tag) {
        log.info("Creating tag: {}", tag.getName());
        CustomerTag created = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * 更新标签
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CRM_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerTag> updateTag(@PathVariable Long id, @RequestBody CustomerTag tag) {
        log.info("Updating tag: {}", id);
        CustomerTag updated = tagService.updateTag(id, tag);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerTag> getTag(@PathVariable Long id) {
        return tagService.getTag(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取所有启用的标签
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<CustomerTag>> getAllEnabledTags() {
        List<CustomerTag> tags = tagService.getAllEnabledTags();
        return ResponseEntity.ok(tags);
    }
    
    /**
     * 按分类获取标签
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CustomerTag>> getTagsByCategory(@PathVariable String category) {
        List<CustomerTag> tags = tagService.getTagsByCategory(category);
        return ResponseEntity.ok(tags);
    }
    
    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerTag>> searchTags(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<CustomerTag> result = tagService.searchTags(name, category, type, enabled, pageable);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取所有标签分类
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = tagService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 删除标签
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CRM_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTag(@PathVariable Long id) {
        log.info("Deleting tag: {}", id);
        tagService.deleteTag(id);
        return ResponseEntity.ok(Map.of("message", "标签删除成功"));
    }
}