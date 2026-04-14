import request from './request'

// 获取平台列表
export function getPlatformList() {
  return request({ url: '/platforms', method: 'get' })
}

// 连接平台
export function connectPlatform(platform, data) {
  return request({ url: `/platforms/${platform}/connect`, method: 'post', data })
}

// 更新平台配置
export function updatePlatformConfig(platform, data) {
  return request({ url: `/platforms/${platform}/config`, method: 'put', data })
}

// 同步平台数据
export function syncPlatformData(platform) {
  return request({ url: `/platforms/${platform}/sync`, method: 'post' })
}