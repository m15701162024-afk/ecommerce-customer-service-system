import request from './request'

export function getServiceHealth() {
  return request({ url: '/actuator/health', method: 'get' })
}

export function getSystemLogs(params) {
  return request({ url: '/monitor/logs', method: 'get', params })
}

export function getRequestTrend(params) {
  return request({ url: '/monitor/request-trend', method: 'get', params })
}

export function getResponseDistribution() {
  return request({ url: '/monitor/response-distribution', method: 'get' })
}