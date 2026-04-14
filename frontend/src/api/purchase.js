import request from './request'

// 获取采购列表
export function getPurchaseList(params) {
  return request({ url: '/purchase/list', method: 'get', params })
}

export function getPurchaseDetail(purchaseId) {
  return request({ url: `/purchase/${purchaseId}`, method: 'get' })
}

// 创建采购订单
export function createPurchase(data) {
  return request({ url: '/purchase', method: 'post', data })
}

// 确认采购
export function confirmPurchase(purchaseId) {
  return request({ url: `/purchase/${purchaseId}/confirm`, method: 'post' })
}

// 拒绝采购
export function rejectPurchase(purchaseId, data) {
  return request({ url: `/purchase/${purchaseId}/reject`, method: 'post', data })
}

// 获取采购统计
export function getPurchaseStats() {
  return request({ url: '/purchase/stats', method: 'get' })
}

// 获取待人工确认列表
export function getManualConfirmList() {
  return request({ url: '/purchase/manual-confirm-list', method: 'get' })
}