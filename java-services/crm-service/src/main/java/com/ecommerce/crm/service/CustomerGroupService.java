package com.ecommerce.crm.service;

import com.ecommerce.crm.entity.CustomerGroup;
import com.ecommerce.crm.repository.CustomerGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 客户分组服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerGroupService {
    
    private final CustomerGroupRepository groupRepository;
    
    /**
     * 创建分组
     */
    @Transactional
    public CustomerGroup createGroup(CustomerGroup group) {
        log.info("Creating group: {}", group.getName());
        
        if (group.getCode() != null && groupRepository.existsByCode(group.getCode())) {
            throw new RuntimeException("分组编码已存在: " + group.getCode());
        }
        
        if (groupRepository.existsByName(group.getName())) {
            throw new RuntimeException("分组名称已存在: " + group.getName());
        }
        
        return groupRepository.save(group);
    }
    
    /**
     * 更新分组
     */
    @Transactional
    public CustomerGroup updateGroup(Long id, CustomerGroup groupUpdate) {
        log.info("Updating group: {}", id);
        
        CustomerGroup group = groupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("分组不存在: " + id));
        
        if (groupUpdate.getName() != null && !groupUpdate.getName().equals(group.getName())) {
            if (groupRepository.existsByName(groupUpdate.getName())) {
                throw new RuntimeException("分组名称已存在: " + groupUpdate.getName());
            }
            group.setName(groupUpdate.getName());
        }
        
        if (groupUpdate.getCode() != null && !groupUpdate.getCode().equals(group.getCode())) {
            if (groupRepository.existsByCode(groupUpdate.getCode())) {
                throw new RuntimeException("分组编码已存在: " + groupUpdate.getCode());
            }
            group.setCode(groupUpdate.getCode());
        }
        
        if (groupUpdate.getDescription() != null) group.setDescription(groupUpdate.getDescription());
        if (groupUpdate.getType() != null) group.setType(groupUpdate.getType());
        if (groupUpdate.getAutoRule() != null) group.setAutoRule(groupUpdate.getAutoRule());
        if (groupUpdate.getDiscountRate() != null) group.setDiscountRate(groupUpdate.getDiscountRate());
        if (groupUpdate.getPointRate() != null) group.setPointRate(groupUpdate.getPointRate());
        if (groupUpdate.getFreeShipping() != null) group.setFreeShipping(groupUpdate.getFreeShipping());
        if (groupUpdate.getExclusiveService() != null) group.setExclusiveService(groupUpdate.getExclusiveService());
        if (groupUpdate.getColor() != null) group.setColor(groupUpdate.getColor());
        if (groupUpdate.getIcon() != null) group.setIcon(groupUpdate.getIcon());
        if (groupUpdate.getSortOrder() != null) group.setSortOrder(groupUpdate.getSortOrder());
        if (groupUpdate.getEnabled() != null) group.setEnabled(groupUpdate.getEnabled());
        
        return groupRepository.save(group);
    }
    
    /**
     * 获取分组详情
     */
    public Optional<CustomerGroup> getGroup(Long id) {
        return groupRepository.findById(id);
    }
    
    /**
     * 按编码获取分组
     */
    public Optional<CustomerGroup> getGroupByCode(String code) {
        return groupRepository.findByCode(code);
    }
    
    /**
     * 获取所有启用的分组
     */
    public List<CustomerGroup> getAllEnabledGroups() {
        return groupRepository.findByEnabledTrueOrderBySortOrderAsc();
    }
    
    /**
     * 获取系统分组
     */
    public List<CustomerGroup> getSystemGroups() {
        return groupRepository.findSystemGroups();
    }
    
    /**
     * 获取自动分组
     */
    public List<CustomerGroup> getAutoGroups() {
        return groupRepository.findAutoGroups();
    }
    
    /**
     * 搜索分组
     */
    public Page<CustomerGroup> searchGroups(String name, String type, Boolean enabled, Pageable pageable) {
        return groupRepository.searchByConditions(name, type, enabled, pageable);
    }
    
    /**
     * 删除分组
     */
    @Transactional
    public void deleteGroup(Long id) {
        log.info("Deleting group: {}", id);
        groupRepository.deleteById(id);
    }
    
    /**
     * 更新分组客户计数
     */
    @Transactional
    public void updateCustomerCount(Long groupId) {
        groupRepository.updateCustomerCount(groupId);
    }
}