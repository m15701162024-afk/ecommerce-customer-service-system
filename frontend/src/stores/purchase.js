import { defineStore } from 'pinia'
import {
  getPurchaseList,
  getPurchaseDetail,
  createPurchase,
  confirmPurchase,
  rejectPurchase,
  getPurchaseStats,
  getManualConfirmList
} from '@/api/purchase'

export const usePurchaseStore = defineStore('purchase', {
  state: () => ({
    purchaseList: [],
    currentPurchase: null,
    total: 0,
    loading: false,
    pagination: {
      pageNum: 1,
      pageSize: 10
    },
    searchParams: {},
    statistics: null,
    manualConfirmList: []
  }),

  getters: {
    hasMore: (state) => state.purchaseList.length < state.total,
    hasCurrentPurchase: (state) => !!state.currentPurchase,
    pendingCount: (state) => state.purchaseList.filter(item => item.status === 'pending').length,
    confirmedCount: (state) => state.purchaseList.filter(item => item.status === 'confirmed').length
  },

  actions: {
    async fetchPurchaseList(params = {}) {
      this.loading = true
      try {
        const queryParams = {
          ...this.pagination,
          ...this.searchParams,
          ...params
        }
        const res = await getPurchaseList(queryParams)
        this.purchaseList = res.data?.list || []
        this.total = res.data?.total || 0
        if (params.pageNum) {
          this.pagination.pageNum = params.pageNum
        }
        if (params.pageSize) {
          this.pagination.pageSize = params.pageSize
        }
      } catch (error) {
        this.purchaseList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    async fetchPurchaseDetail(purchaseId) {
      this.loading = true
      try {
        const res = await getPurchaseDetail(purchaseId)
        this.currentPurchase = res.data
      } catch (error) {
        this.currentPurchase = null
      } finally {
        this.loading = false
      }
    },

    async createPurchaseAction(data) {
      this.loading = true
      try {
        const res = await createPurchase(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async confirmPurchaseAction(purchaseId) {
      this.loading = true
      try {
        const res = await confirmPurchase(purchaseId)
        const index = this.purchaseList.findIndex(item => item.id === purchaseId)
        if (index !== -1) {
          this.purchaseList[index] = { ...this.purchaseList[index], ...res.data }
        }
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async rejectPurchaseAction(purchaseId, data) {
      this.loading = true
      try {
        const res = await rejectPurchase(purchaseId, data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async fetchPurchaseStats() {
      try {
        const res = await getPurchaseStats()
        this.statistics = res.data
      } catch (error) {
        this.statistics = null
      }
    },

    async fetchManualConfirmList() {
      try {
        const res = await getManualConfirmList()
        this.manualConfirmList = res.data || []
      } catch (error) {
        this.manualConfirmList = []
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

    clearCurrentPurchase() {
      this.currentPurchase = null
    }
  }
})