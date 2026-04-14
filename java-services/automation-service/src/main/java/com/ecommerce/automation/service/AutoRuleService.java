package com.ecommerce.automation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.automation.entity.AutoRule;
import com.ecommerce.automation.mapper.AutoRuleMapper;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自动化规则服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoRuleService {

    private final AutoRuleMapper autoRuleMapper;

    /**
     * 创建规则
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(AutoRule rule) {
        // 验证规则配置
        validateRuleConfig(rule);
        
        // 设置默认值
        if (rule.getStatus() == null) {
            rule.setStatus("ENABLED");
        }
        if (rule.getPriority() == null) {
            rule.setPriority(100);
        }
        if (rule.getExecutionCount() == null) {
            rule.setExecutionCount(0L);
        }
        
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setDeleted(0);
        
        autoRuleMapper.insert(rule);
        log.info("创建自动化规则成功, id={}, name={}, type={}", rule.getId(), rule.getName(), rule.getRuleType());
        
        return rule.getId();
    }

    /**
     * 更新规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(Long id, AutoRule rule) {
        AutoRule existing = getRuleById(id);
        
        // 验证规则配置
        validateRuleConfig(rule);
        
        rule.setId(id);
        rule.setUpdatedAt(LocalDateTime.now());
        
        autoRuleMapper.updateById(rule);
        log.info("更新自动化规则成功, id={}", id);
    }

    /**
     * 删除规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        AutoRule rule = getRuleById(id);
        autoRuleMapper.deleteById(id);
        log.info("删除自动化规则成功, id={}, name={}", id, rule.getName());
    }

    /**
     * 启用规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableRule(Long id) {
        AutoRule rule = getRuleById(id);
        rule.setStatus("ENABLED");
        rule.setUpdatedAt(LocalDateTime.now());
        autoRuleMapper.updateById(rule);
        log.info("启用自动化规则成功, id={}", id);
    }

    /**
     * 禁用规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableRule(Long id) {
        AutoRule rule = getRuleById(id);
        rule.setStatus("DISABLED");
        rule.setUpdatedAt(LocalDateTime.now());
        autoRuleMapper.updateById(rule);
        log.info("禁用自动化规则成功, id={}", id);
    }

    /**
     * 获取规则详情
     */
    public AutoRule getRuleById(Long id) {
        AutoRule rule = autoRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        return rule;
    }

    /**
     * 分页查询规则
     */
    public PageResult<AutoRule> listRules(Integer pageNum, Integer pageSize, String ruleType, Long shopId, String status) {
        Page<AutoRule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AutoRule> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(ruleType)) {
            wrapper.eq(AutoRule::getRuleType, ruleType);
        }
        if (shopId != null) {
            wrapper.eq(AutoRule::getShopId, shopId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AutoRule::getStatus, status);
        }
        
        wrapper.orderByAsc(AutoRule::getPriority);
        wrapper.orderByDesc(AutoRule::getCreatedAt);
        
        Page<AutoRule> result = autoRuleMapper.selectPage(page, wrapper);
        
        return PageResult.of(
            result.getRecords(),
            result.getTotal(),
            result.getCurrent(),
            result.getSize()
        );
    }

    /**
     * 获取启用的规则(按优先级排序)
     */
    public List<AutoRule> getEnabledRules() {
        return autoRuleMapper.findEnabledRulesOrderByPriority();
    }

    /**
     * 获取指定店铺的启用的规则
     */
    public List<AutoRule> getEnabledRulesByShopId(Long shopId) {
        return autoRuleMapper.findEnabledRulesByShopId(shopId);
    }

    /**
     * 获取指定类型的启用的规则
     */
    public List<AutoRule> getEnabledRulesByType(String ruleType) {
        return autoRuleMapper.findEnabledRulesByType(ruleType);
    }

    /**
     * 增加执行次数
     */
    @Transactional(rollbackFor = Exception.class)
    public void incrementExecutionCount(Long ruleId) {
        autoRuleMapper.incrementExecutionCount(ruleId);
    }

    /**
     * 验证规则配置
     */
    private void validateRuleConfig(AutoRule rule) {
        if (!StringUtils.hasText(rule.getName())) {
            throw new BusinessException("规则名称不能为空");
        }
        if (!StringUtils.hasText(rule.getRuleType())) {
            throw new BusinessException("规则类型不能为空");
        }
        
        // 验证规则类型
        if (!List.of("KEYWORD", "TIME", "EVENT").contains(rule.getRuleType())) {
            throw new BusinessException("无效的规则类型: " + rule.getRuleType());
        }
        
        // 验证触发配置
        if (rule.getTriggerConfig() == null || rule.getTriggerConfig().isEmpty()) {
            throw new BusinessException("触发条件配置不能为空");
        }
        
        // 验证动作配置
        if (rule.getActionConfig() == null || rule.getActionConfig().isEmpty()) {
            throw new BusinessException("执行动作配置不能为空");
        }
        
        // 根据规则类型验证具体配置
        switch (rule.getRuleType()) {
            case "KEYWORD" -> validateKeywordConfig(rule.getTriggerConfig());
            case "TIME" -> validateTimeConfig(rule.getTriggerConfig());
            case "EVENT" -> validateEventConfig(rule.getTriggerConfig());
        }
    }

    /**
     * 验证关键词规则配置
     */
    private void validateKeywordConfig(java.util.Map<String, Object> config) {
        if (!config.containsKey("keywords")) {
            throw new BusinessException("关键词配置必须包含keywords字段");
        }
        Object keywords = config.get("keywords");
        if (!(keywords instanceof List) || ((List<?>) keywords).isEmpty()) {
            throw new BusinessException("关键词列表不能为空");
        }
    }

    /**
     * 验证定时规则配置
     */
    private void validateTimeConfig(java.util.Map<String, Object> config) {
        if (!config.containsKey("cron")) {
            throw new BusinessException("定时配置必须包含cron表达式");
        }
        // 可以添加cron表达式验证
    }

    /**
     * 验证事件规则配置
     */
    private void validateEventConfig(java.util.Map<String, Object> config) {
        if (!config.containsKey("eventType")) {
            throw new BusinessException("事件配置必须包含eventType字段");
        }
    }
}