package com.ecommerce.automation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.automation.entity.WorkflowDefinition;
import com.ecommerce.automation.entity.WorkflowInstance;
import com.ecommerce.automation.mapper.WorkflowDefinitionMapper;
import com.ecommerce.automation.mapper.WorkflowInstanceMapper;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowDefinitionMapper definitionMapper;
    private final WorkflowInstanceMapper instanceMapper;

    // ==================== 工作流定义管理 ====================

    /**
     * 创建工作流定义
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createWorkflowDefinition(WorkflowDefinition definition) {
        validateWorkflowDefinition(definition);
        
        // 检查编码唯一性
        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowDefinition::getCode, definition.getCode());
        if (definitionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("工作流编码已存在: " + definition.getCode());
        }
        
        // 设置默认值
        if (definition.getStatus() == null) {
            definition.setStatus("DRAFT");
        }
        if (definition.getVersion() == null) {
            definition.setVersion(1);
        }
        
        definition.setCreatedAt(LocalDateTime.now());
        definition.setUpdatedAt(LocalDateTime.now());
        definition.setDeleted(0);
        
        definitionMapper.insert(definition);
        log.info("创建工作流定义成功, id={}, code={}", definition.getId(), definition.getCode());
        
        return definition.getId();
    }

    /**
     * 更新工作流定义
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkflowDefinition(Long id, WorkflowDefinition definition) {
        WorkflowDefinition existing = getWorkflowDefinitionById(id);
        
        validateWorkflowDefinition(definition);
        
        definition.setId(id);
        definition.setUpdatedAt(LocalDateTime.now());
        
        definitionMapper.updateById(definition);
        log.info("更新工作流定义成功, id={}", id);
    }

    /**
     * 发布工作流定义
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishWorkflowDefinition(Long id) {
        WorkflowDefinition definition = getWorkflowDefinitionById(id);
        
        // 验证工作流定义完整性
        if (definition.getSteps() == null || definition.getSteps().isEmpty()) {
            throw new BusinessException("工作流步骤不能为空");
        }
        if (!StringUtils.hasText(definition.getStartStepId())) {
            throw new BusinessException("起始步骤ID不能为空");
        }
        
        definition.setStatus("PUBLISHED");
        definition.setUpdatedAt(LocalDateTime.now());
        
        definitionMapper.updateById(definition);
        log.info("发布工作流定义成功, id={}, code={}", id, definition.getCode());
    }

    /**
     * 归档工作流定义
     */
    @Transactional(rollbackFor = Exception.class)
    public void archiveWorkflowDefinition(Long id) {
        WorkflowDefinition definition = getWorkflowDefinitionById(id);
        definition.setStatus("ARCHIVED");
        definition.setUpdatedAt(LocalDateTime.now());
        
        definitionMapper.updateById(definition);
        log.info("归档工作流定义成功, id={}", id);
    }

    /**
     * 获取工作流定义详情
     */
    public WorkflowDefinition getWorkflowDefinitionById(Long id) {
        WorkflowDefinition definition = definitionMapper.selectById(id);
        if (definition == null) {
            throw new BusinessException("工作流定义不存在");
        }
        return definition;
    }

    /**
     * 根据编码获取工作流定义
     */
    public WorkflowDefinition getWorkflowDefinitionByCode(String code) {
        WorkflowDefinition definition = definitionMapper.findByCode(code);
        if (definition == null) {
            throw new BusinessException("工作流定义不存在: " + code);
        }
        return definition;
    }

    /**
     * 分页查询工作流定义
     */
    public PageResult<WorkflowDefinition> listWorkflowDefinitions(Integer pageNum, Integer pageSize, Long shopId, String status) {
        Page<WorkflowDefinition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<>();
        
        if (shopId != null) {
            wrapper.eq(WorkflowDefinition::getShopId, shopId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(WorkflowDefinition::getStatus, status);
        }
        
        wrapper.orderByDesc(WorkflowDefinition::getCreatedAt);
        
        Page<WorkflowDefinition> result = definitionMapper.selectPage(page, wrapper);
        
        return PageResult.of(
            result.getRecords(),
            result.getTotal(),
            result.getCurrent(),
            result.getSize()
        );
    }

    // ==================== 工作流实例管理 ====================

    /**
     * 启动工作流实例
     */
    @Transactional(rollbackFor = Exception.class)
    public Long startWorkflowInstance(Long definitionId, Long sessionId, Long shopId, Long userId, 
                                       String businessId, String businessType, String triggerSource) {
        WorkflowDefinition definition = getWorkflowDefinitionById(definitionId);
        
        if (!"PUBLISHED".equals(definition.getStatus())) {
            throw new BusinessException("工作流定义未发布，无法启动");
        }
        
        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowDefinitionId(definitionId);
        instance.setDefinitionVersion(definition.getVersion());
        instance.setSessionId(sessionId);
        instance.setShopId(shopId);
        instance.setUserId(userId);
        instance.setBusinessId(businessId);
        instance.setBusinessType(businessType);
        instance.setTriggerSource(triggerSource);
        instance.setCurrentStepId(definition.getStartStepId());
        instance.setStatus("RUNNING");
        instance.setContext(new HashMap<>());
        instance.setRetryCount(0);
        instance.setStartedAt(LocalDateTime.now());
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setDeleted(0);
        
        // 设置超时时间 (默认30分钟)
        instance.setTimeoutAt(LocalDateTime.now().plusMinutes(30));
        
        instanceMapper.insert(instance);
        log.info("启动工作流实例成功, instanceId={}, definitionId={}, definitionCode={}", 
                 instance.getId(), definitionId, definition.getCode());
        
        return instance.getId();
    }

    /**
     * 暂停工作流实例
     */
    @Transactional(rollbackFor = Exception.class)
    public void pauseWorkflowInstance(Long instanceId) {
        WorkflowInstance instance = getWorkflowInstanceById(instanceId);
        
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new BusinessException("只有运行中的工作流才能暂停");
        }
        
        instance.setStatus("PAUSED");
        instance.setUpdatedAt(LocalDateTime.now());
        instanceMapper.updateById(instance);
        
        log.info("暂停工作流实例成功, instanceId={}", instanceId);
    }

    /**
     * 恢复工作流实例
     */
    @Transactional(rollbackFor = Exception.class)
    public void resumeWorkflowInstance(Long instanceId) {
        WorkflowInstance instance = getWorkflowInstanceById(instanceId);
        
        if (!"PAUSED".equals(instance.getStatus())) {
            throw new BusinessException("只有暂停的工作流才能恢复");
        }
        
        instance.setStatus("RUNNING");
        instance.setUpdatedAt(LocalDateTime.now());
        instanceMapper.updateById(instance);
        
        log.info("恢复工作流实例成功, instanceId={}", instanceId);
    }

    /**
     * 取消工作流实例
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelWorkflowInstance(Long instanceId) {
        WorkflowInstance instance = getWorkflowInstanceById(instanceId);
        
        instance.setStatus("CANCELLED");
        instance.setFinishedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        instanceMapper.updateById(instance);
        
        log.info("取消工作流实例成功, instanceId={}", instanceId);
    }

    /**
     * 更新工作流实例上下文
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateInstanceContext(Long instanceId, Map<String, Object> context) {
        WorkflowInstance instance = getWorkflowInstanceById(instanceId);
        
        Map<String, Object> existingContext = instance.getContext();
        if (existingContext == null) {
            existingContext = new HashMap<>();
        }
        existingContext.putAll(context);
        instance.setContext(existingContext);
        instance.setUpdatedAt(LocalDateTime.now());
        
        instanceMapper.updateById(instance);
    }

    /**
     * 获取工作流实例详情
     */
    public WorkflowInstance getWorkflowInstanceById(Long id) {
        WorkflowInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("工作流实例不存在");
        }
        return instance;
    }

    /**
     * 获取会话的工作流实例
     */
    public List<WorkflowInstance> getInstancesBySessionId(Long sessionId) {
        return instanceMapper.findBySessionId(sessionId);
    }

    /**
     * 分页查询工作流实例
     */
    public PageResult<WorkflowInstance> listWorkflowInstances(Integer pageNum, Integer pageSize, 
                                                              Long definitionId, Long shopId, String status) {
        Page<WorkflowInstance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WorkflowInstance> wrapper = new LambdaQueryWrapper<>();
        
        if (definitionId != null) {
            wrapper.eq(WorkflowInstance::getWorkflowDefinitionId, definitionId);
        }
        if (shopId != null) {
            wrapper.eq(WorkflowInstance::getShopId, shopId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(WorkflowInstance::getStatus, status);
        }
        
        wrapper.orderByDesc(WorkflowInstance::getCreatedAt);
        
        Page<WorkflowInstance> result = instanceMapper.selectPage(page, wrapper);
        
        return PageResult.of(
            result.getRecords(),
            result.getTotal(),
            result.getCurrent(),
            result.getSize()
        );
    }

    /**
     * 验证工作流定义
     */
    private void validateWorkflowDefinition(WorkflowDefinition definition) {
        if (!StringUtils.hasText(definition.getName())) {
            throw new BusinessException("工作流名称不能为空");
        }
        if (!StringUtils.hasText(definition.getCode())) {
            throw new BusinessException("工作流编码不能为空");
        }
    }
}