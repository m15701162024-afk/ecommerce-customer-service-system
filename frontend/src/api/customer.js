import request from './request'

// ==================== 客户CRUD ====================

/**
 * 创建客户
 */
export function createCustomer(data) {
  return request({ url: '/customers', method: 'post', data })
}

/**
 * 更新客户
 */
export function updateCustomer(id, data) {
  return request({ url: `/customers/${id}`, method: 'put', data })
}

/**
 * 获取客户详情
 */
export function getCustomer(id) {
  return request({ url: `/customers/${id}`, method: 'get' })
}

/**
 * 删除客户
 */
export function deleteCustomer(id) {
  return request({ url: `/customers/${id}`, method: 'delete' })
}

/**
 * 获取客户列表
 */
export function getCustomers(params) {
  return request({ url: '/customers', method: 'get', params })
}

// ==================== 客户搜索 ====================

/**
 * 搜索客户
 */
export function searchCustomers(params) {
  return request({ url: '/customers/search', method: 'get', params })
}

/**
 * 按等级获取客户
 */
export function getCustomersByLevel(level, params) {
  return request({ url: `/customers/level/${level}`, method: 'get', params })
}

/**
 * 按活跃度获取客户
 */
export function getCustomersByActivity(activity, params) {
  return request({ url: `/customers/activity/${activity}`, method: 'get', params })
}

/**
 * 获取高价值客户
 */
export function getHighValueCustomers(params) {
  return request({ url: '/customers/high-value', method: 'get', params })
}

// ==================== 标签管理 ====================

/**
 * 添加标签
 */
export function addTags(customerId, tagIds) {
  return request({ url: `/customers/${customerId}/tags`, method: 'post', data: tagIds })
}

/**
 * 移除标签
 */
export function removeTags(customerId, tagIds) {
  return request({ url: `/customers/${customerId}/tags`, method: 'delete', data: tagIds })
}

/**
 * 批量添加标签
 */
export function batchAddTags(data) {
  return request({ url: '/customers/tags/batch-add', method: 'post', data })
}

/**
 * 批量移除标签
 */
export function batchRemoveTags(data) {
  return request({ url: '/customers/tags/batch-remove', method: 'post', data })
}

// ==================== 分组管理 ====================

/**
 * 设置客户分组
 */
export function setCustomerGroup(customerId, groupId) {
  return request({ url: `/customers/${customerId}/group`, method: 'put', data: { groupId } })
}

// ==================== 等级管理 ====================

/**
 * 更新客户等级
 */
export function updateCustomerLevel(customerId, level) {
  return request({ url: `/customers/${customerId}/level`, method: 'put', data: { level } })
}

// ==================== 黑名单管理 ====================

/**
 * 加入黑名单
 */
export function addToBlacklist(customerId) {
  return request({ url: `/customers/${customerId}/blacklist`, method: 'post' })
}

/**
 * 移出黑名单
 */
export function removeFromBlacklist(customerId) {
  return request({ url: `/customers/${customerId}/blacklist`, method: 'delete' })
}

// ==================== 统计分析 ====================

/**
 * 获取客户统计
 */
export function getCustomerStatistics() {
  return request({ url: '/customers/statistics', method: 'get' })
}

// ==================== 标签API ====================

/**
 * 获取所有启用的标签
 */
export function getEnabledTags() {
  return request({ url: '/tags/enabled', method: 'get' })
}

/**
 * 获取标签列表
 */
export function getTags(params) {
  return request({ url: '/tags/search', method: 'get', params })
}

/**
 * 创建标签
 */
export function createTag(data) {
  return request({ url: '/tags', method: 'post', data })
}

/**
 * 更新标签
 */
export function updateTag(id, data) {
  return request({ url: `/tags/${id}`, method: 'put', data })
}

/**
 * 删除标签
 */
export function deleteTag(id) {
  return request({ url: `/tags/${id}`, method: 'delete' })
}

/**
 * 获取标签分类
 */
export function getTagCategories() {
  return request({ url: '/tags/categories', method: 'get' })
}

/**
 * 按分类获取标签
 */
export function getTagsByCategory(category) {
  return request({ url: `/tags/category/${category}`, method: 'get' })
}

// ==================== 分组API ====================

/**
 * 获取所有启用的分组
 */
export function getEnabledGroups() {
  return request({ url: '/groups/enabled', method: 'get' })
}

/**
 * 获取分组列表
 */
export function getGroups(params) {
  return request({ url: '/groups/search', method: 'get', params })
}

/**
 * 创建分组
 */
export function createGroup(data) {
  return request({ url: '/groups', method: 'post', data })
}

/**
 * 更新分组
 */
export function updateGroup(id, data) {
  return request({ url: `/groups/${id}`, method: 'put', data })
}

/**
 * 删除分组
 */
export function deleteGroup(id) {
  return request({ url: `/groups/${id}`, method: 'delete' })
}

/**
 * 获取系统分组
 */
export function getSystemGroups() {
  return request({ url: '/groups/system', method: 'get' })
}

/**
 * 获取自动分组
 */
export function getAutoGroups() {
  return request({ url: '/groups/auto', method: 'get' })
}

// ==================== 客户等级常量 ====================

export const CustomerLevels = {
  NORMAL: { code: 'NORMAL', name: '普通客户', color: '#909399' },
  SILVER: { code: 'SILVER', name: '银卡会员', color: '#C0C0C0' },
  GOLD: { code: 'GOLD', name: '金卡会员', color: '#FFD700' },
  PLATINUM: { code: 'PLATINUM', name: '白金会员', color: '#E5E4E2' },
  DIAMOND: { code: 'DIAMOND', name: '钻石会员', color: '#B9F2FF' }
}

// ==================== 客户活跃度常量 ====================

export const CustomerActivities = {
  INACTIVE: { code: 'INACTIVE', name: '不活跃', color: '#F56C6C' },
  LOW: { code: 'LOW', name: '低活跃', color: '#E6A23C' },
  NORMAL: { code: 'NORMAL', name: '一般', color: '#909399' },
  ACTIVE: { code: 'ACTIVE', name: '活跃', color: '#67C23A' },
  HIGHLY_ACTIVE: { code: 'HIGHLY_ACTIVE', name: '高活跃', color: '#409EFF' }
}