import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 - 添加Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // 返回res.data，这样调用者可以直接访问token等字段
    return res.data || res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request