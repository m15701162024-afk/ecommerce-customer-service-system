import request from './request'

// 获取工单列表
export function getWorkOrderList(params) {
  return request({ url: '/work-orders', method: 'get', params })
}

// 获取工单详情
export function getWorkOrderDetail(id) {
  return request({ url: `/work-orders/${id}`, method: 'get' })
}

// 创建工单
export function createWorkOrder(data) {
  return request({ url: '/work-orders', method: 'post', data })
}

// 分配工单
export function assignWorkOrder(data) {
  return request({ url: '/work-orders/assign', method: 'post', data })
}

// 转派工单
export function transferWorkOrder(data) {
  return request({ url: '/work-orders/transfer', method: 'post', data })
}

// 开始处理工单
export function startProcessingWorkOrder(id) {
  return request({ url: `/work-orders/${id}/start-processing`, method: 'post' })
}

// 解决工单
export function resolveWorkOrder(data) {
  return request({ url: '/work-orders/resolve', method: 'post', data })
}

// 关闭工单
export function closeWorkOrder(data) {
  return request({ url: '/work-orders/close', method: 'post', data })
}

// 重开工单
export function reopenWorkOrder(id, reason) {
  return request({ 
    url: `/work-orders/${id}/reopen`, 
    method: 'post', 
    params: { reason } 
  })
}

// 获取工单统计
export function getWorkOrderStats(params) {
  return request({ url: '/work-orders/stats', method: 'get', params })
}

// 获取超时工单列表
export function getOverdueWorkOrders() {
  return request({ url: '/work-orders/overdue', method: 'get' })
}

// 获取即将超时工单列表
export function getNearDueWorkOrders(hoursBefore = 1) {
  return request({ 
    url: '/work-orders/near-due', 
    method: 'get', 
    params: { hoursBefore } 
  })
}