import request from './request'

// 登录
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

// 登出
export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}

// 获取用户信息
export function getUserInfo() {
  return request({ url: '/user/info', method: 'get' })
}

export function refreshToken() {
  return request({ url: '/auth/refresh', method: 'post' })
}

export function register(data) {
  return request({ url: '/auth/register', method: 'post', data })
}

export function sendSmsCode(phone) {
  return request({ url: '/auth/sms-code', method: 'post', data: { phone } })
}

export function loginByPhone(data) {
  return request({ url: '/auth/login-phone', method: 'post', data })
}