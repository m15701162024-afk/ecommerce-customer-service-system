package com.ecommerce.crm.service;

import com.ecommerce.crm.dto.*;
import com.ecommerce.crm.entity.Customer;
import com.ecommerce.crm.entity.CustomerGroup;
import com.ecommerce.crm.entity.CustomerTag;
import com.ecommerce.crm.enums.CustomerActivity;
import com.ecommerce.crm.enums.CustomerLevel;
import com.ecommerce.crm.repository.CustomerBehaviorLogRepository;
import com.ecommerce.crm.repository.CustomerGroupRepository;
import com.ecommerce.crm.repository.CustomerRepository;
import com.ecommerce.crm.repository.CustomerTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final CustomerTagRepository tagRepository;
    private final CustomerGroupRepository groupRepository;
    private final CustomerBehaviorLogRepository behaviorLogRepository;
    
    /**
     * 创建客户
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        log.info("Creating customer: {}", request.getName());
        
        // 检查手机号是否已存在
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已存在: " + request.getPhone());
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + request.getEmail());
        }
        
        Customer customer = Customer.builder()
            .name(request.getName())
            .phone(request.getPhone())
            .email(request.getEmail())
            .wechat(request.getWechat())
            .avatar(request.getAvatar())
            .gender(request.getGender())
            .birthday(request.getBirthday())
            .address(request.getAddress())
            .province(request.getProvince())
            .city(request.getCity())
            .district(request.getDistrict())
            .source(request.getSource())
            .externalId(request.getExternalId())
            .storeId(request.getStoreId())
            .remark(request.getRemark())
            .serviceStaffId(request.getServiceStaffId())
            .serviceStaffName(request.getServiceStaffName())
            .level(CustomerLevel.NORMAL)
            .activity(CustomerActivity.NORMAL)
            .enabled(true)
            .isBlacklist(false)
            .build();
        
        // 设置分组
        if (request.getGroupId() != null) {
            CustomerGroup group = groupRepository.findById(request.getGroupId())
                .orElse(null);
            customer.setGroup(group);
        }
        
        customer = customerRepository.save(customer);
        log.info("Customer created with ID: {}", customer.getId());
        
        return toResponse(customer);
    }
    
    /**
     * 更新客户
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        log.info("Updating customer: {}", id);
        
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("客户不存在: " + id));
        
        // 更新基本信息
        if (request.getName() != null) customer.setName(request.getName());
        if (request.getPhone() != null) {
            if (!request.getPhone().equals(customer.getPhone()) && 
                customerRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("手机号已存在: " + request.getPhone());
            }
            customer.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(customer.getEmail()) && 
                customerRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已存在: " + request.getEmail());
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getWechat() != null) customer.setWechat(request.getWechat());
        if (request.getAvatar() != null) customer.setAvatar(request.getAvatar());
        if (request.getGender() != null) customer.setGender(request.getGender());
        if (request.getBirthday() != null) customer.setBirthday(request.getBirthday());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getProvince() != null) customer.setProvince(request.getProvince());
        if (request.getCity() != null) customer.setCity(request.getCity());
        if (request.getDistrict() != null) customer.setDistrict(request.getDistrict());
        if (request.getRemark() != null) customer.setRemark(request.getRemark());
        if (request.getEnabled() != null) customer.setEnabled(request.getEnabled());
        if (request.getIsBlacklist() != null) customer.setIsBlacklist(request.getIsBlacklist());
        if (request.getServiceStaffId() != null) customer.setServiceStaffId(request.getServiceStaffId());
        if (request.getServiceStaffName() != null) customer.setServiceStaffName(request.getServiceStaffName());
        
        // 更新分组
        if (request.getGroupId() != null) {
            CustomerGroup group = groupRepository.findById(request.getGroupId())
                .orElse(null);
            customer.setGroup(group);
        }
        
        customer = customerRepository.save(customer);
        log.info("Customer updated: {}", id);
        
        return toResponse(customer);
    }
    
    /**
     * 获取客户详情
     */
    public CustomerResponse getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("客户不存在: " + id));
        return toResponse(customer);
    }
    
    /**
     * 删除客户
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer: {}", id);
        customerRepository.deleteById(id);
    }
    
    /**
     * 搜索客户
     */
    public Page<CustomerResponse> searchCustomers(String keyword, CustomerLevel level, 
            CustomerActivity activity, String source, Long storeId, Boolean isBlacklist,
            Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return customerRepository.searchByKeyword(keyword, pageable)
                .map(this::toResponse);
        }
        return customerRepository.searchByConditions(null, null, level, activity, source, storeId, isBlacklist, pageable)
            .map(this::toResponse);
    }
    
    /**
     * 按等级获取客户
     */
    public Page<CustomerResponse> getCustomersByLevel(CustomerLevel level, Pageable pageable) {
        return customerRepository.findByLevel(level, pageable)
            .map(this::toResponse);
    }
    
    /**
     * 按活跃度获取客户
     */
    public Page<CustomerResponse> getCustomersByActivity(CustomerActivity activity, Pageable pageable) {
        return customerRepository.findByActivity(activity, pageable)
            .map(this::toResponse);
    }
    
    /**
     * 获取高价值客户
     */
    public List<CustomerResponse> getHighValueCustomers(BigDecimal minAmount, int limit) {
        return customerRepository.findHighValueCustomers(minAmount, PageRequest.of(0, limit))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 添加标签
     */
    @Transactional
    public void addTags(Long customerId, List<Long> tagIds) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("客户不存在: " + customerId));
        
        Set<CustomerTag> tags = new HashSet<>(tagRepository.findByIdIn(new HashSet<>(tagIds)));
        customer.getTags().addAll(tags);
        customerRepository.save(customer);
        
        // 更新标签客户数
        tagIds.forEach(tagId -> tagRepository.updateCustomerCount(tagId));
    }
    
    /**
     * 移除标签
     */
    @Transactional
    public void removeTags(Long customerId, List<Long> tagIds) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("客户不存在: " + customerId));
        
        Set<CustomerTag> tagsToRemove = customer.getTags().stream()
            .filter(tag -> tagIds.contains(tag.getId()))
            .collect(Collectors.toSet());
        customer.getTags().removeAll(tagsToRemove);
        customerRepository.save(customer);
        
        // 更新标签客户数
        tagIds.forEach(tagId -> tagRepository.updateCustomerCount(tagId));
    }
    
    /**
     * 批量添加标签
     */
    @Transactional
    public void batchAddTags(CustomerTagOperationRequest request) {
        List<Customer> customers = customerRepository.findAllById(request.getCustomerIds());
        Set<CustomerTag> tags = new HashSet<>(tagRepository.findByIdIn(new HashSet<>(request.getTagIds())));
        
        for (Customer customer : customers) {
            customer.getTags().addAll(tags);
        }
        
        customerRepository.saveAll(customers);
        
        // 更新标签客户数
        request.getTagIds().forEach(tagId -> tagRepository.updateCustomerCount(tagId));
    }
    
    /**
     * 批量移除标签
     */
    @Transactional
    public void batchRemoveTags(CustomerTagOperationRequest request) {
        List<Customer> customers = customerRepository.findAllById(request.getCustomerIds());
        Set<Long> tagIdSet = new HashSet<>(request.getTagIds());
        
        for (Customer customer : customers) {
            customer.getTags().removeIf(tag -> tagIdSet.contains(tag.getId()));
        }
        
        customerRepository.saveAll(customers);
        
        // 更新标签客户数
        request.getTagIds().forEach(tagId -> tagRepository.updateCustomerCount(tagId));
    }
    
    /**
     * 设置客户分组
     */
    @Transactional
    public void setGroup(Long customerId, Long groupId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("客户不存在: " + customerId));
        
        CustomerGroup group = groupId != null ? 
            groupRepository.findById(groupId).orElse(null) : null;
        
        customer.setGroup(group);
        customerRepository.save(customer);
    }
    
    /**
     * 更新客户等级
     */
    @Transactional
    public void updateLevel(Long customerId, CustomerLevel level) {
        customerRepository.updateLevel(customerId, level);
    }
    
    /**
     * 加入黑名单
     */
    @Transactional
    public void addToBlacklist(Long customerId) {
        customerRepository.updateBlacklistStatus(customerId, true);
    }
    
    /**
     * 移出黑名单
     */
    @Transactional
    public void removeFromBlacklist(Long customerId) {
        customerRepository.updateBlacklistStatus(customerId, false);
    }
    
    /**
     * 获取客户统计
     */
    public CustomerStatisticsResponse getStatistics() {
        Long total = customerRepository.count();
        Long blacklist = customerRepository.countByLevel(CustomerLevel.DIAMOND); // 临时用
        
        // 等级分布
        List<CustomerStatisticsResponse.LevelDistribution> levelDistribution = new ArrayList<>();
        long totalForPercentage = total > 0 ? total : 1;
        for (CustomerLevel level : CustomerLevel.values()) {
            Long count = customerRepository.countByLevel(level);
            levelDistribution.add(CustomerStatisticsResponse.LevelDistribution.builder()
                .level(level)
                .levelName(level.getDescription())
                .count(count)
                .percentage(BigDecimal.valueOf(count * 100.0 / totalForPercentage).setScale(2, RoundingMode.HALF_UP))
                .build());
        }
        
        // 活跃度分布
        List<CustomerStatisticsResponse.ActivityDistribution> activityDistribution = new ArrayList<>();
        for (CustomerActivity activity : CustomerActivity.values()) {
            Long count = customerRepository.countByActivity(activity);
            activityDistribution.add(CustomerStatisticsResponse.ActivityDistribution.builder()
                .activity(activity)
                .activityName(activity.getDescription())
                .count(count)
                .percentage(BigDecimal.valueOf(count * 100.0 / totalForPercentage).setScale(2, RoundingMode.HALF_UP))
                .build());
        }
        
        // 新增客户统计
        LocalDateTime now = LocalDateTime.now();
        Long todayNew = customerRepository.countByCreatedAtBetween(now.toLocalDate().atStartOfDay(), now);
        Long weekNew = customerRepository.countByCreatedAtBetween(now.minusWeeks(1), now);
        Long monthNew = customerRepository.countByCreatedAtBetween(now.minusMonths(1), now);
        
        // 消费统计
        BigDecimal totalAmount = customerRepository.sumTotalAmount();
        BigDecimal avgAmount = customerRepository.avgTotalAmount();
        
        return CustomerStatisticsResponse.builder()
            .totalCustomers(total)
            .enabledCustomers(total)
            .blacklistCustomers(blacklist)
            .levelDistribution(levelDistribution)
            .activityDistribution(activityDistribution)
            .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
            .avgAmount(avgAmount != null ? avgAmount : BigDecimal.ZERO)
            .todayNewCustomers(todayNew)
            .weekNewCustomers(weekNew)
            .monthNewCustomers(monthNew)
            .build();
    }
    
    /**
     * 转换为响应DTO
     */
    private CustomerResponse toResponse(Customer customer) {
        Set<CustomerResponse.TagInfo> tags = customer.getTags().stream()
            .map(tag -> CustomerResponse.TagInfo.builder()
                .id(tag.getId())
                .name(tag.getName())
                .code(tag.getCode())
                .category(tag.getCategory())
                .color(tag.getColor())
                .build())
            .collect(Collectors.toSet());
        
        CustomerResponse.GroupInfo group = customer.getGroup() != null ?
            CustomerResponse.GroupInfo.builder()
                .id(customer.getGroup().getId())
                .name(customer.getGroup().getName())
                .code(customer.getGroup().getCode())
                .build() : null;
        
        return CustomerResponse.builder()
            .id(customer.getId())
            .name(customer.getName())
            .phone(customer.getPhone())
            .email(customer.getEmail())
            .wechat(customer.getWechat())
            .avatar(customer.getAvatar())
            .gender(customer.getGender())
            .birthday(customer.getBirthday())
            .address(customer.getAddress())
            .province(customer.getProvince())
            .city(customer.getCity())
            .district(customer.getDistrict())
            .source(customer.getSource())
            .externalId(customer.getExternalId())
            .storeId(customer.getStoreId())
            .level(customer.getLevel())
            .levelDescription(customer.getLevel().getDescription())
            .totalOrders(customer.getTotalOrders())
            .totalAmount(customer.getTotalAmount())
            .avgOrderAmount(customer.getAvgOrderAmount())
            .maxOrderAmount(customer.getMaxOrderAmount())
            .refundAmount(customer.getRefundAmount())
            .refundCount(customer.getRefundCount())
            .activity(customer.getActivity())
            .activityDescription(customer.getActivity().getDescription())
            .lastOrderTime(customer.getLastOrderTime())
            .lastLoginTime(customer.getLastLoginTime())
            .lastConsultTime(customer.getLastConsultTime())
            .loginCount(customer.getLoginCount())
            .consultCount(customer.getConsultCount())
            .consumptionPreference(customer.getConsumptionPreference())
            .priceSensitivity(customer.getPriceSensitivity())
            .brandPreference(customer.getBrandPreference())
            .remark(customer.getRemark())
            .tags(tags)
            .group(group)
            .rfmScore(customer.calculateRfmScore())
            .enabled(customer.getEnabled())
            .isBlacklist(customer.getIsBlacklist())
            .serviceStaffId(customer.getServiceStaffId())
            .serviceStaffName(customer.getServiceStaffName())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }
}