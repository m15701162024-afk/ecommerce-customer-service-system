package com.ecommerce.crm.service;

import com.ecommerce.crm.entity.CustomerTag;
import com.ecommerce.crm.repository.CustomerTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 客户标签服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerTagService {
    
    private final CustomerTagRepository tagRepository;
    
    /**
     * 创建标签
     */
    @Transactional
    public CustomerTag createTag(CustomerTag tag) {
        log.info("Creating tag: {}", tag.getName());
        
        if (tag.getCode() != null && tagRepository.existsByCode(tag.getCode())) {
            throw new RuntimeException("标签编码已存在: " + tag.getCode());
        }
        
        if (tagRepository.existsByName(tag.getName())) {
            throw new RuntimeException("标签名称已存在: " + tag.getName());
        }
        
        return tagRepository.save(tag);
    }
    
    /**
     * 更新标签
     */
    @Transactional
    public CustomerTag updateTag(Long id, CustomerTag tagUpdate) {
        log.info("Updating tag: {}", id);
        
        CustomerTag tag = tagRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("标签不存在: " + id));
        
        if (tagUpdate.getName() != null && !tagUpdate.getName().equals(tag.getName())) {
            if (tagRepository.existsByName(tagUpdate.getName())) {
                throw new RuntimeException("标签名称已存在: " + tagUpdate.getName());
            }
            tag.setName(tagUpdate.getName());
        }
        
        if (tagUpdate.getCode() != null && !tagUpdate.getCode().equals(tag.getCode())) {
            if (tagRepository.existsByCode(tagUpdate.getCode())) {
                throw new RuntimeException("标签编码已存在: " + tagUpdate.getCode());
            }
            tag.setCode(tagUpdate.getCode());
        }
        
        if (tagUpdate.getDescription() != null) tag.setDescription(tagUpdate.getDescription());
        if (tagUpdate.getCategory() != null) tag.setCategory(tagUpdate.getCategory());
        if (tagUpdate.getColor() != null) tag.setColor(tagUpdate.getColor());
        if (tagUpdate.getIcon() != null) tag.setIcon(tagUpdate.getIcon());
        if (tagUpdate.getType() != null) tag.setType(tagUpdate.getType());
        if (tagUpdate.getAutoRule() != null) tag.setAutoRule(tagUpdate.getAutoRule());
        if (tagUpdate.getSortOrder() != null) tag.setSortOrder(tagUpdate.getSortOrder());
        if (tagUpdate.getEnabled() != null) tag.setEnabled(tagUpdate.getEnabled());
        
        return tagRepository.save(tag);
    }
    
    /**
     * 获取标签详情
     */
    public Optional<CustomerTag> getTag(Long id) {
        return tagRepository.findById(id);
    }
    
    /**
     * 按编码获取标签
     */
    public Optional<CustomerTag> getTagByCode(String code) {
        return tagRepository.findByCode(code);
    }
    
    /**
     * 获取所有启用的标签
     */
    public List<CustomerTag> getAllEnabledTags() {
        return tagRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    /**
     * 按分类获取标签
     */
    public List<CustomerTag> getTagsByCategory(String category) {
        return tagRepository.findByCategory(category);
    }
    
    /**
     * 搜索标签
     */
    public Page<CustomerTag> searchTags(String name, String category, String type, Boolean enabled, Pageable pageable) {
        return tagRepository.searchByConditions(name, category, type, enabled, pageable);
    }
    
    /**
     * 获取所有标签分类
     */
    public List<String> getAllCategories() {
        return tagRepository.findAllCategories();
    }
    
    /**
     * 删除标签
     */
    @Transactional
    public void deleteTag(Long id) {
        log.info("Deleting tag: {}", id);
        tagRepository.deleteById(id);
    }
    
    /**
     * 更新标签客户计数
     */
    @Transactional
    public void updateCustomerCount(Long tagId) {
        tagRepository.updateCustomerCount(tagId);
    }
    
    /**
     * 批量更新所有标签客户计数
     */
    @Transactional
    public void updateAllCustomerCounts() {
        tagRepository.updateAllCustomerCounts();
    }
}