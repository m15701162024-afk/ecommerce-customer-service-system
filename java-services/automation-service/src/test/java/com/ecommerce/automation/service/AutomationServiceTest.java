package com.ecommerce.automation.service;

import com.ecommerce.automation.entity.AutoRule;
import com.ecommerce.automation.entity.ExecutionLog;
import com.ecommerce.automation.executor.RuleExecutor;
import com.ecommerce.automation.mapper.AutoRuleMapper;
import com.ecommerce.automation.mapper.ExecutionLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 自动化服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AutomationServiceTest {

    @Mock
    private AutoRuleMapper autoRuleMapper;

    @Mock
    private ExecutionLogMapper executionLogMapper;

    @Mock
    private RuleExecutor ruleExecutor;

    @InjectMocks
    private AutoRuleService autoRuleService;

    @InjectMocks
    private TriggerService triggerService;

    private AutoRule testRule;

    @BeforeEach
    void setUp() {
        testRule = createTestRule();
    }

    @Test
    @DisplayName("创建规则 - 成功")
    void createRule_Success() {
        // Given
        when(autoRuleMapper.insert(any(AutoRule.class))).thenAnswer(invocation -> {
            AutoRule rule = invocation.getArgument(0);
            rule.setId(1L);
            return 1;
        });

        // When
        Long result = autoRuleService.createRule(testRule);

        // Then
        assertNotNull(result);
        verify(autoRuleMapper, times(1)).insert(any(AutoRule.class));
    }

    @Test
    @DisplayName("更新规则 - 成功")
    void updateRule_Success() {
        // Given
        when(autoRuleMapper.selectById(1L)).thenReturn(testRule);
        when(autoRuleMapper.updateById(any(AutoRule.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            autoRuleService.updateRule(1L, testRule));
    }

    @Test
    @DisplayName("删除规则 - 成功")
    void deleteRule_Success() {
        // Given
        when(autoRuleMapper.selectById(1L)).thenReturn(testRule);
        when(autoRuleMapper.deleteById(1L)).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            autoRuleService.deleteRule(1L));
    }

    @Test
    @DisplayName("启用规则 - 成功")
    void enableRule_Success() {
        // Given
        testRule.setStatus("DISABLED");
        when(autoRuleMapper.selectById(1L)).thenReturn(testRule);
        when(autoRuleMapper.updateById(any(AutoRule.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            autoRuleService.enableRule(1L));
    }

    @Test
    @DisplayName("禁用规则 - 成功")
    void disableRule_Success() {
        // Given
        testRule.setStatus("ENABLED");
        when(autoRuleMapper.selectById(1L)).thenReturn(testRule);
        when(autoRuleMapper.updateById(any(AutoRule.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            autoRuleService.disableRule(1L));
    }

    @Test
    @DisplayName("获取启用的规则列表 - 成功")
    void getEnabledRules_Success() {
        // Given
        List<AutoRule> rules = new ArrayList<>();
        rules.add(testRule);
        when(autoRuleMapper.findEnabledRulesByType("KEYWORD")).thenReturn(rules);

        // When
        List<AutoRule> result = autoRuleMapper.findEnabledRulesByType("KEYWORD");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("关键词触发处理 - 匹配成功")
    void processKeywordTrigger_Matched() {
        // Given
        List<AutoRule> rules = new ArrayList<>();
        rules.add(testRule);
        
        when(autoRuleMapper.findEnabledRulesByType("KEYWORD")).thenReturn(rules);
        doNothing().when(ruleExecutor).executeRule(any(), any(), any(), any(), any(), any());

        // When
        boolean result = triggerService.processKeywordTrigger(1L, 1L, 1L, "价格");

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("关键词触发处理 - 不匹配")
    void processKeywordTrigger_NotMatched() {
        // Given
        List<AutoRule> rules = new ArrayList<>();
        rules.add(testRule);
        
        when(autoRuleMapper.findEnabledRulesByType("KEYWORD")).thenReturn(rules);

        // When
        boolean result = triggerService.processKeywordTrigger(1L, 1L, 1L, "随便说说");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("记录执行日志 - 成功")
    void logExecution_Success() {
        // Given
        when(executionLogMapper.insert(any(ExecutionLog.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            triggerService.logExecution(
                "RULE", 1L, 1L, 1L, 1L,
                "KEYWORD", "价格", "REPLY", null,
                "SUCCESS", "执行成功", 100L
            ));
    }

    @Test
    @DisplayName("增加执行次数 - 成功")
    void incrementExecutionCount_Success() {
        // Given
        when(autoRuleMapper.updateExecutionCount(1L)).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> 
            autoRuleService.incrementExecutionCount(1L));
    }

    @Test
    @DisplayName("规则有效期检查 - 在有效期内")
    void isRuleEffective_WithinRange() {
        // Given
        testRule.setEffectiveFrom(LocalDateTime.now().minusDays(1));
        testRule.setEffectiveTo(LocalDateTime.now().plusDays(1));

        // When
        boolean isEffective = isWithinEffectivePeriod(testRule);

        // Then
        assertTrue(isEffective);
    }

    @Test
    @DisplayName("规则有效期检查 - 已过期")
    void isRuleEffective_Expired() {
        // Given
        testRule.setEffectiveTo(LocalDateTime.now().minusDays(1));

        // When
        boolean isEffective = isWithinEffectivePeriod(testRule);

        // Then
        assertFalse(isEffective);
    }

    // Helper methods

    private AutoRule createTestRule() {
        AutoRule rule = new AutoRule();
        rule.setId(1L);
        rule.setName("价格咨询自动回复");
        rule.setRuleType("KEYWORD");
        rule.setShopId(1L);
        rule.setStatus("ENABLED");
        rule.setPriority(100);
        rule.setExecutionCount(0L);
        
        Map<String, Object> triggerConfig = new HashMap<>();
        triggerConfig.put("keywords", List.of("价格", "多少钱", "优惠"));
        triggerConfig.put("matchType", "FUZZY");
        rule.setTriggerConfig(triggerConfig);
        
        Map<String, Object> actionConfig = new HashMap<>();
        actionConfig.put("actionType", "REPLY");
        actionConfig.put("content", "亲，这款商品现在活动价xxx元哦~");
        rule.setActionConfig(actionConfig);
        
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        
        return rule;
    }

    private boolean isWithinEffectivePeriod(AutoRule rule) {
        LocalDateTime now = LocalDateTime.now();
        
        if (rule.getEffectiveFrom() != null && now.isBefore(rule.getEffectiveFrom())) {
            return false;
        }
        
        if (rule.getEffectiveTo() != null && now.isAfter(rule.getEffectiveTo())) {
            return false;
        }
        
        return true;
    }
}