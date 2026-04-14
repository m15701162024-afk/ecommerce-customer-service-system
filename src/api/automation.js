/**
 * 自动化服务 API
 * 用于管理自动回复规则、工作流和触发器
 */
import request from '@/utils/request'

const API_PREFIX = '/api/automation'

// ==================== 规则管理 ====================

/**
 * 创建自动回复规则
 * @param {Object} data - 规则数据
 * @param {string} data.name - 规则名称
 * @param {string} data.ruleType - 规则类型 KEYWORD|TIME|EVENT
 * @param {number} data.shopId - 店铺ID (可选, null表示全局规则)
 * @param {Object} data.triggerConfig - 触发条件配置
 * @param {Object} data.actionConfig - 执行动作配置
 * @param {number} data.priority - 优先级 (数值越小优先级越高)
 * @param {string} data.description - 描述
 */
export function createRule(data) {
  return request({
    url: `${API_PREFIX}/rules`,
    method: 'post',
    data
  })
}

/**
 * 更新自动回复规则
 * @param {number} id - 规则ID
 * @param {Object} data - 规则数据
 */
export function updateRule(id, data) {
  return request({
    url: `${API_PREFIX}/rules/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除自动回复规则
 * @param {number} id - 规则ID
 */
export function deleteRule(id) {
  return request({
    url: `${API_PREFIX}/rules/${id}`,
    method: 'delete'
  })
}

/**
 * 启用规则
 * @param {number} id - 规则ID
 */
export function enableRule(id) {
  return request({
    url: `${API_PREFIX}/rules/${id}/enable`,
    method: 'put'
  })
}

/**
 * 禁用规则
 * @param {number} id - 规则ID
 */
export function disableRule(id) {
  return request({
    url: `${API_PREFIX}/rules/${id}/disable`,
    method: 'put'
  })
}

/**
 * 获取规则详情
 * @param {number} id - 规则ID
 */
export function getRule(id) {
  return request({
    url: `${API_PREFIX}/rules/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询规则列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.ruleType - 规则类型
 * @param {number} params.shopId - 店铺ID
 * @param {string} params.status - 状态
 */
export function listRules(params) {
  return request({
    url: `${API_PREFIX}/rules`,
    method: 'get',
    params
  })
}

/**
 * 查询规则执行日志
 * @param {number} ruleId - 规则ID
 * @param {number} limit - 限制条数
 */
export function getRuleExecutionLogs(ruleId, limit = 100) {
  return request({
    url: `${API_PREFIX}/rules/${ruleId}/logs`,
    method: 'get',
    params: { limit }
  })
}

// ==================== 工作流管理 ====================

/**
 * 创建工作流定义
 * @param {Object} data - 工作流数据
 * @param {string} data.name - 工作流名称
 * @param {string} data.code - 工作流编码
 * @param {number} data.shopId - 店铺ID
 * @param {string} data.description - 描述
 * @param {Object} data.triggerCondition - 触发条件
 * @param {Array} data.steps - 工作流步骤
 * @param {Object} data.variables - 变量定义
 * @param {string} data.startStepId - 起始步骤ID
 */
export function createWorkflow(data) {
  return request({
    url: `${API_PREFIX}/workflows`,
    method: 'post',
    data
  })
}

/**
 * 更新工作流定义
 * @param {number} id - 工作流ID
 * @param {Object} data - 工作流数据
 */
export function updateWorkflow(id, data) {
  return request({
    url: `${API_PREFIX}/workflows/${id}`,
    method: 'put',
    data
  })
}

/**
 * 发布工作流
 * @param {number} id - 工作流ID
 */
export function publishWorkflow(id) {
  return request({
    url: `${API_PREFIX}/workflows/${id}/publish`,
    method: 'put'
  })
}

/**
 * 归档工作流
 * @param {number} id - 工作流ID
 */
export function archiveWorkflow(id) {
  return request({
    url: `${API_PREFIX}/workflows/${id}/archive`,
    method: 'put'
  })
}

/**
 * 获取工作流定义详情
 * @param {number} id - 工作流ID
 */
export function getWorkflow(id) {
  return request({
    url: `${API_PREFIX}/workflows/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询工作流定义列表
 * @param {Object} params - 查询参数
 */
export function listWorkflows(params) {
  return request({
    url: `${API_PREFIX}/workflows`,
    method: 'get',
    params
  })
}

// ==================== 工作流实例 ====================

/**
 * 启动工作流实例
 * @param {number} definitionId - 工作流定义ID
 * @param {Object} params - 启动参数
 * @param {number} params.sessionId - 会话ID
 * @param {number} params.shopId - 店铺ID
 * @param {number} params.userId - 用户ID
 * @param {string} params.businessId - 业务ID
 * @param {string} params.businessType - 业务类型
 * @param {string} params.triggerSource - 触发来源
 */
export function startWorkflow(definitionId, params) {
  return request({
    url: `${API_PREFIX}/workflows/${definitionId}/start`,
    method: 'post',
    params
  })
}

/**
 * 暂停工作流实例
 * @param {number} instanceId - 实例ID
 */
export function pauseWorkflowInstance(instanceId) {
  return request({
    url: `${API_PREFIX}/instances/${instanceId}/pause`,
    method: 'put'
  })
}

/**
 * 恢复工作流实例
 * @param {number} instanceId - 实例ID
 */
export function resumeWorkflowInstance(instanceId) {
  return request({
    url: `${API_PREFIX}/instances/${instanceId}/resume`,
    method: 'put'
  })
}

/**
 * 取消工作流实例
 * @param {number} instanceId - 实例ID
 */
export function cancelWorkflowInstance(instanceId) {
  return request({
    url: `${API_PREFIX}/instances/${instanceId}/cancel`,
    method: 'put'
  })
}

/**
 * 获取工作流实例详情
 * @param {number} instanceId - 实例ID
 */
export function getWorkflowInstance(instanceId) {
  return request({
    url: `${API_PREFIX}/instances/${instanceId}`,
    method: 'get'
  })
}

/**
 * 查询工作流实例列表
 * @param {Object} params - 查询参数
 */
export function listWorkflowInstances(params) {
  return request({
    url: `${API_PREFIX}/instances`,
    method: 'get',
    params
  })
}

/**
 * 查询工作流执行日志
 * @param {number} instanceId - 实例ID
 */
export function getWorkflowExecutionLogs(instanceId) {
  return request({
    url: `${API_PREFIX}/instances/${instanceId}/logs`,
    method: 'get'
  })
}

// ==================== 触发器测试 ====================

/**
 * 测试关键词触发
 * @param {Object} params - 测试参数
 * @param {number} params.sessionId - 会话ID
 * @param {number} params.shopId - 店铺ID
 * @param {number} params.userId - 用户ID
 * @param {string} params.message - 测试消息
 */
export function testKeywordTrigger(params) {
  return request({
    url: `${API_PREFIX}/test/keyword`,
    method: 'post',
    params
  })
}

/**
 * 触发欢迎语
 * @param {Object} params - 参数
 * @param {number} params.sessionId - 会话ID
 * @param {number} params.shopId - 店铺ID
 * @param {number} params.userId - 用户ID
 */
export function triggerWelcome(params) {
  return request({
    url: `${API_PREFIX}/test/welcome`,
    method: 'post',
    params
  })
}

/**
 * 发送订单状态通知
 * @param {Object} params - 参数
 * @param {number} params.shopId - 店铺ID
 * @param {number} params.userId - 用户ID
 * @param {string} params.orderNo - 订单号
 * @param {string} params.oldStatus - 旧状态
 * @param {string} params.newStatus - 新状态
 */
export function sendOrderNotification(params) {
  return request({
    url: `${API_PREFIX}/test/order-notification`,
    method: 'post',
    params
  })
}

// ==================== 规则类型常量 ====================

export const RuleType = {
  KEYWORD: 'KEYWORD',  // 关键词触发
  TIME: 'TIME',        // 定时触发
  EVENT: 'EVENT'       // 事件触发
}

export const RuleStatus = {
  ENABLED: 'ENABLED',
  DISABLED: 'DISABLED'
}

export const WorkflowStatus = {
  DRAFT: 'DRAFT',
  PUBLISHED: 'PUBLISHED',
  ARCHIVED: 'ARCHIVED'
}

export const InstanceStatus = {
  RUNNING: 'RUNNING',
  PAUSED: 'PAUSED',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
  CANCELLED: 'CANCELLED'
}

export const ActionType = {
  REPLY: 'REPLY',       // 自动回复
  NOTIFY: 'NOTIFY',     // 发送通知
  TRANSFER: 'TRANSFER', // 转接客服
  WEBHOOK: 'WEBHOOK'    // 调用Webhook
}

export const EventType = {
  ORDER_CREATED: 'ORDER_CREATED',
  PAYMENT_SUCCESS: 'PAYMENT_SUCCESS',
  ORDER_SHIPPED: 'ORDER_SHIPPED',
  ORDER_COMPLETED: 'ORDER_COMPLETED',
  FIRST_CONSULTATION: 'FIRST_CONSULTATION',
  ORDER_STATUS_CHANGE: 'ORDER_STATUS_CHANGE'
}