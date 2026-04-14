import request from './request'

export function getOrderList(params) {
  return request({ url: '/orders/list', method: 'get', params })
}

export function getOrderDetail(orderId) {
  return request({ url: `/orders/${orderId}`, method: 'get' })
}

export function updateOrderStatus(orderId, status) {
  return request({ url: `/orders/${orderId}/status`, method: 'put', data: { status } })
}

export function syncOrders(platform) {
  return request({ url: '/orders/sync', method: 'post', data: { platform } })
}

export function getOrderTrend(params) {
  return request({ url: '/orders/trend', method: 'get', params })
}

export function getPlatformDistribution() {
  return request({ url: '/orders/platform-distribution', method: 'get' })
}