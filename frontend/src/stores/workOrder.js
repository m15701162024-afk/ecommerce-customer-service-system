import { defineStore } from 'pinia'
import {
  getWorkOrderList,
  getWorkOrderDetail,
  createWorkOrder,
  assignWorkOrder,
  transferWorkOrder,
  startProcessingWorkOrder,
  resolveWorkOrder,
  closeWorkOrder,
  reopenWorkOrder,
  getWorkOrderStats,
  getOverdueWorkOrders,
  getNearDueWorkOrders
} from '@/api/workOrder'

export const useWorkOrderStore = defineStore('workOrder', {
  state: () => ({
    workOrderList: [],
    currentWorkOrder: null,
    total: 0,
    loading: false,
    pagination: {
      pageNum: 1,
      pageSize: 10
    },
    searchParams: {},
    statistics: null,
    overdueList: [],
    nearDueList: []
  }),

  getters: {
    hasMore: (state) => state.workOrderList.length < state.total,
    hasCurrentWorkOrder: (state) => !!state.currentWorkOrder,
    pendingCount: (state) => state.workOrderList.filter(item => item.status === 'pending').length,
    processingCount: (state) => state.workOrderList.filter(item => item.status === 'processing').length
  },

  actions: {
    async fetchWorkOrderList(params = {}) {
      this.loading = true
      try {
        const queryParams = {
          ...this.pagination,
          ...this.searchParams,
          ...params
        }
        const res = await getWorkOrderList(queryParams)
        this.workOrderList = res.data?.list || []
        this.total = res.data?.total || 0
        if (params.pageNum) {
          this.pagination.pageNum = params.pageNum
        }
        if (params.pageSize) {
          this.pagination.pageSize = params.pageSize
        }
      } catch (error) {
        this.workOrderList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    async fetchWorkOrderDetail(id) {
      this.loading = true
      try {
        const res = await getWorkOrderDetail(id)
        this.currentWorkOrder = res.data
      } catch (error) {
        this.currentWorkOrder = null
      } finally {
        this.loading = false
      }
    },

    async createWorkOrderAction(data) {
      this.loading = true
      try {
        const res = await createWorkOrder(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async assignWorkOrderAction(data) {
      this.loading = true
      try {
        const res = await assignWorkOrder(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async transferWorkOrderAction(data) {
      this.loading = true
      try {
        const res = await transferWorkOrder(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async startProcessingAction(id) {
      this.loading = true
      try {
        const res = await startProcessingWorkOrder(id)
        if (this.currentWorkOrder?.id === id) {
          this.currentWorkOrder = { ...this.currentWorkOrder, ...res.data }
        }
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async resolveWorkOrderAction(data) {
      this.loading = true
      try {
        const res = await resolveWorkOrder(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async closeWorkOrderAction(data) {
      this.loading = true
      try {
        const res = await closeWorkOrder(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async reopenWorkOrderAction(id, reason) {
      this.loading = true
      try {
        const res = await reopenWorkOrder(id, reason)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async fetchWorkOrderStats(params = {}) {
      try {
        const res = await getWorkOrderStats(params)
        this.statistics = res.data
      } catch (error) {
        this.statistics = null
      }
    },

    async fetchOverdueWorkOrders() {
      try {
        const res = await getOverdueWorkOrders()
        this.overdueList = res.data || []
      } catch (error) {
        this.overdueList = []
      }
    },

    async fetchNearDueWorkOrders(hoursBefore = 1) {
      try {
        const res = await getNearDueWorkOrders(hoursBefore)
        this.nearDueList = res.data || []
      } catch (error) {
        this.nearDueList = []
      }
    },

    setSearchParams(params) {
      this.searchParams = params
      this.pagination.pageNum = 1
    },

    resetSearchParams() {
      this.searchParams = {}
      this.pagination.pageNum = 1
    },

    clearCurrentWorkOrder() {
      this.currentWorkOrder = null
    }
  }
})