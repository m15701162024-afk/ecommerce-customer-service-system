import { defineStore } from 'pinia'
import {
  getServiceHealth,
  getSystemLogs,
  getRequestTrend,
  getResponseDistribution
} from '@/api/monitor'

export const useMonitorStore = defineStore('monitor', {
  state: () => ({
    healthStatus: null,
    systemLogs: [],
    requestTrend: [],
    responseDistribution: null,
    loading: false,
    pagination: {
      pageNum: 1,
      pageSize: 50
    },
    logFilters: {
      level: '',
      startTime: '',
      endTime: ''
    }
  }),

  getters: {
    isHealthy: (state) => state.healthStatus?.status === 'UP',
    errorLogs: (state) => state.systemLogs.filter(log => log.level === 'ERROR'),
    warningLogs: (state) => state.systemLogs.filter(log => log.level === 'WARN'),
    totalLogs: (state) => state.systemLogs.length
  },

  actions: {
    async fetchServiceHealth() {
      this.loading = true
      try {
        const res = await getServiceHealth()
        this.healthStatus = res.data
      } catch (error) {
        console.error('获取服务健康状态失败:', error)
        this.healthStatus = { status: 'DOWN', error: error.message }
      } finally {
        this.loading = false
      }
    },

    async fetchSystemLogs(params = {}) {
      this.loading = true
      try {
        const queryParams = {
          ...this.pagination,
          ...this.logFilters,
          ...params
        }
        const res = await getSystemLogs(queryParams)
        this.systemLogs = res.data?.list || []
      } catch (error) {
        console.error('获取系统日志失败:', error)
        this.systemLogs = []
      } finally {
        this.loading = false
      }
    },

    async fetchRequestTrend(params = {}) {
      this.loading = true
      try {
        const res = await getRequestTrend(params)
        this.requestTrend = res.data || []
      } catch (error) {
        console.error('获取请求趋势失败:', error)
        this.requestTrend = []
      } finally {
        this.loading = false
      }
    },

    async fetchResponseDistribution() {
      this.loading = true
      try {
        const res = await getResponseDistribution()
        this.responseDistribution = res.data
      } catch (error) {
        console.error('获取响应分布失败:', error)
        this.responseDistribution = null
      } finally {
        this.loading = false
      }
    },

    setLogFilters(filters) {
      this.logFilters = { ...this.logFilters, ...filters }
      this.pagination.pageNum = 1
    },

    resetLogFilters() {
      this.logFilters = {
        level: '',
        startTime: '',
        endTime: ''
      }
      this.pagination.pageNum = 1
    },

    setPagination(pageNum, pageSize) {
      this.pagination.pageNum = pageNum
      this.pagination.pageSize = pageSize
    }
  }
})