import request from './request'

// ==================== 工单系统 ====================

// 创建工单
export function createWorkOrder(data) {
  return request({ url: '/work-orders', method: 'post', data })
}

// 获取工单列表
export function getWorkOrderList(params) {
  return request({ url: '/work-orders', method: 'get', params })
}

// 获取工单详情
export function getWorkOrderDetail(id) {
  return request({ url: `/work-orders/${id}`, method: 'get' })
}

// 分配工单
export function assignWorkOrder(id, data) {
  return request({ url: `/work-orders/${id}/assign`, method: 'put', data })
}

// 转派工单
export function transferWorkOrder(id, data) {
  return request({ url: `/work-orders/${id}/transfer`, method: 'post', data })
}

// 解决工单
export function resolveWorkOrder(id, data) {
  return request({ url: `/work-orders/${id}/resolve`, method: 'put', data })
}

// 关闭工单
export function closeWorkOrder(id) {
  return request({ url: `/work-orders/${id}/close`, method: 'put' })
}

// 获取工单统计
export function getWorkOrderStatistics(params) {
  return request({ url: '/work-orders/statistics', method: 'get', params })
}

// 获取工单分类
export function getWorkOrderCategories() {
  return request({ url: '/work-orders/categories', method: 'get' })
}

// ==================== 售后处理 ====================

// 申请售后
export function createAfterSale(data) {
  return request({ url: '/after-sales', method: 'post', data })
}

// 获取售后列表
export function getAfterSaleList(params) {
  return request({ url: '/after-sales', method: 'get', params })
}

// 获取售后详情
export function getAfterSaleDetail(id) {
  return request({ url: `/after-sales/${id}`, method: 'get' })
}

// 审批通过
export function approveAfterSale(id, data) {
  return request({ url: `/after-sales/${id}/approve`, method: 'put', data })
}

// 审批拒绝
export function rejectAfterSale(id, data) {
  return request({ url: `/after-sales/${id}/reject`, method: 'put', data })
}

// 填写退货物流
export function fillReturnLogistics(id, data) {
  return request({ url: `/after-sales/${id}/logistics`, method: 'put', data })
}

// 确认收货
export function confirmReceive(id) {
  return request({ url: `/after-sales/${id}/receive`, method: 'put' })
}

// 执行退款
export function refundAfterSale(id, data) {
  return request({ url: `/after-sales/${id}/refund`, method: 'post', data })
}

// 获取售后原因列表
export function getAfterSaleReasons(type) {
  return request({ url: '/after-sales/reasons', method: 'get', params: { type } })
}

// 获取售后统计
export function getAfterSaleStatistics(params) {
  return request({ url: '/after-sales/statistics', method: 'get', params })
}

// ==================== 消息模板 ====================

// 获取模板列表
export function getTemplateList(params) {
  return request({ url: '/templates', method: 'get', params })
}

// 创建模板
export function createTemplate(data) {
  return request({ url: '/templates', method: 'post', data })
}

// 更新模板
export function updateTemplate(id, data) {
  return request({ url: `/templates/${id}`, method: 'put', data })
}

// 删除模板
export function deleteTemplate(id) {
  return request({ url: `/templates/${id}`, method: 'delete' })
}

// 获取模板分类
export function getTemplateCategories() {
  return request({ url: '/templates/categories', method: 'get' })
}

// ==================== 自动化规则 ====================

// 获取规则列表
export function getAutoRuleList(params) {
  return request({ url: '/auto-rules', method: 'get', params })
}

// 创建规则
export function createAutoRule(data) {
  return request({ url: '/auto-rules', method: 'post', data })
}

// 更新规则
export function updateAutoRule(id, data) {
  return request({ url: `/auto-rules/${id}`, method: 'put', data })
}

// 删除规则
export function deleteAutoRule(id) {
  return request({ url: `/auto-rules/${id}`, method: 'delete' })
}

// 切换规则状态
export function toggleAutoRule(id) {
  return request({ url: `/auto-rules/${id}/toggle`, method: 'put' })
}

// 测试规则
export function testAutoRule(id, data) {
  return request({ url: `/auto-rules/${id}/test`, method: 'post', data })
}

// ==================== CRM客户管理 ====================

// 获取客户列表
export function getCustomerList(params) {
  return request({ url: '/customers', method: 'get', params })
}

// 获取客户详情
export function getCustomerDetail(id) {
  return request({ url: `/customers/${id}`, method: 'get' })
}

// 获取客户档案(按买家ID)
export function getCustomerByBuyerId(buyerId, platform) {
  return request({ url: `/customers/buyer/${buyerId}`, method: 'get', params: { platform } })
}

// 更新客户信息
export function updateCustomer(id, data) {
  return request({ url: `/customers/${id}`, method: 'put', data })
}

// 添加客户标签
export function addCustomerTag(customerId, tagId) {
  return request({ url: `/customers/${customerId}/tags`, method: 'post', data: { tagId } })
}

// 移除客户标签
export function removeCustomerTag(customerId, tagId) {
  return request({ url: `/customers/${customerId}/tags/${tagId}`, method: 'delete' })
}

// 获取客户标签列表
export function getCustomerTags() {
  return request({ url: '/customers/tags', method: 'get' })
}

// 创建客户标签
export function createCustomerTag(data) {
  return request({ url: '/customers/tags', method: 'post', data })
}

// 获取客户分组
export function getCustomerGroups() {
  return request({ url: '/customers/groups', method: 'get' })
}

// 获取客户订单历史
export function getCustomerOrders(customerId) {
  return request({ url: `/customers/${customerId}/orders`, method: 'get' })
}

// 获取客户统计
export function getCustomerStatistics(params) {
  return request({ url: '/customers/statistics', method: 'get', params })
}

// ==================== 质检监控 ====================

// 获取质检记录列表
export function getQualityCheckList(params) {
  return request({ url: '/quality/checks', method: 'get', params })
}

// 获取质检详情
export function getQualityCheckDetail(id) {
  return request({ url: `/quality/checks/${id}`, method: 'get' })
}

// 创建质检记录
export function createQualityCheck(data) {
  return request({ url: '/quality/checks', method: 'post', data })
}

// 确认质检结果
export function confirmQualityCheck(id) {
  return request({ url: `/quality/checks/${id}/confirm`, method: 'put' })
}

// 申诉
export function appealQualityCheck(id, data) {
  return request({ url: `/quality/checks/${id}/appeal`, method: 'post', data })
}

// 获取质检规则
export function getQualityRules() {
  return request({ url: '/quality/rules', method: 'get' })
}

// 创建质检规则
export function createQualityRule(data) {
  return request({ url: '/quality/rules', method: 'post', data })
}

// 获取敏感词列表
export function getSensitiveWords(params) {
  return request({ url: '/quality/sensitive-words', method: 'get', params })
}

// 添加敏感词
export function addSensitiveWord(data) {
  return request({ url: '/quality/sensitive-words', method: 'post', data })
}

// 删除敏感词
export function deleteSensitiveWord(id) {
  return request({ url: `/quality/sensitive-words/${id}`, method: 'delete' })
}

// 检测敏感词
export function checkSensitiveWord(text) {
  return request({ url: '/quality/sensitive-words/check', method: 'post', data: { text } })
}

// ==================== 会话评价 ====================

// 提交评价
export function submitSessionRating(sessionId, data) {
  return request({ url: `/sessions/${sessionId}/rating`, method: 'post', data })
}

// 获取评价详情
export function getSessionRating(sessionId) {
  return request({ url: `/sessions/${sessionId}/rating`, method: 'get' })
}

// 获取评价统计
export function getRatingStatistics(params) {
  return request({ url: '/ratings/statistics', method: 'get', params })
}

// 获取评价标签
export function getRatingTags() {
  return request({ url: '/ratings/tags', method: 'get' })
}

// ==================== 客服排班绩效 ====================

// 获取排班列表
export function getScheduleList(params) {
  return request({ url: '/schedules', method: 'get', params })
}

// 创建排班
export function createSchedule(data) {
  return request({ url: '/schedules', method: 'post', data })
}

// 批量创建排班
export function batchCreateSchedule(data) {
  return request({ url: '/schedules/batch', method: 'post', data })
}

// 更新排班
export function updateSchedule(id, data) {
  return request({ url: `/schedules/${id}`, method: 'put', data })
}

// 删除排班
export function deleteSchedule(id) {
  return request({ url: `/schedules/${id}`, method: 'delete' })
}

// 获取客服状态
export function getAgentStatus() {
  return request({ url: '/agents/status', method: 'get' })
}

// 更新客服状态
export function updateAgentStatus(data) {
  return request({ url: '/agents/status', method: 'put', data })
}

// 获取绩效数据
export function getAgentPerformance(params) {
  return request({ url: '/agents/performance', method: 'get', params })
}

// 获取客服排行榜
export function getAgentRanking(params) {
  return request({ url: '/agents/ranking', method: 'get', params })
}

// ==================== 数据分析 ====================

// 获取运营概览
export function getAnalyticsOverview(params) {
  return request({ url: '/analytics/overview', method: 'get', params })
}

// 获取客服统计
export function getAnalyticsCustomerService(params) {
  return request({ url: '/analytics/customer-service', method: 'get', params })
}

// 获取销售分析
export function getAnalyticsSales(params) {
  return request({ url: '/analytics/sales', method: 'get', params })
}

// 获取商品分析
export function getAnalyticsProducts(params) {
  return request({ url: '/analytics/products', method: 'get', params })
}

// 获取趋势数据
export function getAnalyticsTrend(type, params) {
  return request({ url: `/analytics/trend/${type}`, method: 'get', params })
}

// ==================== 商品推荐 ====================

// 获取推荐商品
export function getRecommendProducts(data) {
  return request({ url: '/recommend/products', method: 'post', data })
}

// 获取热销商品推荐
export function getHotProducts(params) {
  return request({ url: '/recommend/hot', method: 'get', params })
}

// 获取关联商品推荐
export function getRelatedProducts(productId) {
  return request({ url: `/recommend/related/${productId}`, method: 'get' })
}

// 获取推荐规则列表
export function getRecommendRules(params) {
  return request({ url: '/recommend/rules', method: 'get', params })
}

// 创建推荐规则
export function createRecommendRule(data) {
  return request({ url: '/recommend/rules', method: 'post', data })
}