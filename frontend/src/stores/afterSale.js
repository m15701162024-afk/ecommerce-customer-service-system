import { defineStore } from 'pinia'
import {
  getAfterSaleList,
  getAfterSaleDetail,
  applyAfterSale,
  approveAfterSale,
  fillReturnLogistics,
  confirmReturnReceived,
  executeRefund,
  shipExchangeGoods,
  cancelAfterSale,
  addSellerRemark,
  getAfterSaleReasons,
  getAfterSaleStatistics,
  getReturnAddresses
} from '@/api/afterSale'

export const useAfterSaleStore = defineStore('afterSale', {
  state: () => ({
    afterSaleList: [],
    currentAfterSale: null,
    total: 0,
    loading: false,
    pagination: {
      pageNum: 1,
      pageSize: 10
    },
    searchParams: {},
    reasons: [],
    statistics: null,
    returnAddresses: []
  }),

  getters: {
    hasMore: (state) => state.afterSaleList.length < state.total,
    hasCurrentAfterSale: (state) => !!state.currentAfterSale,
    pendingCount: (state) => state.afterSaleList.filter(item => item.status === 'pending').length
  },

  actions: {
    async fetchAfterSaleList(params = {}) {
      this.loading = true
      try {
        const queryParams = {
          ...this.pagination,
          ...this.searchParams,
          ...params
        }
        const res = await getAfterSaleList(queryParams)
        this.afterSaleList = res.data?.list || []
        this.total = res.data?.total || 0
        if (params.pageNum) {
          this.pagination.pageNum = params.pageNum
        }
        if (params.pageSize) {
          this.pagination.pageSize = params.pageSize
        }
      } catch (error) {
        this.afterSaleList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    async fetchAfterSaleDetail(id) {
      this.loading = true
      try {
        const res = await getAfterSaleDetail(id)
        this.currentAfterSale = res.data
      } catch (error) {
        this.currentAfterSale = null
      } finally {
        this.loading = false
      }
    },

    async applyAfterSaleAction(data) {
      this.loading = true
      try {
        const res = await applyAfterSale(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async approveAfterSaleAction(id, data) {
      this.loading = true
      try {
        const res = await approveAfterSale(id, data)
        if (this.currentAfterSale?.id === id) {
          this.currentAfterSale = { ...this.currentAfterSale, ...res.data }
        }
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async fillReturnLogisticsAction(id, data) {
      this.loading = true
      try {
        const res = await fillReturnLogistics(id, data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async confirmReturnReceivedAction(id) {
      this.loading = true
      try {
        const res = await confirmReturnReceived(id)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async executeRefundAction(id) {
      this.loading = true
      try {
        const res = await executeRefund(id)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async shipExchangeGoodsAction(id, data) {
      this.loading = true
      try {
        const res = await shipExchangeGoods(id, data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async cancelAfterSaleAction(id) {
      this.loading = true
      try {
        await cancelAfterSale(id)
        const index = this.afterSaleList.findIndex(item => item.id === id)
        if (index !== -1) {
          this.afterSaleList[index].status = 'cancelled'
        }
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    async addSellerRemarkAction(id, remark) {
      try {
        await addSellerRemark(id, remark)
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    async fetchAfterSaleReasons(type) {
      try {
        const res = await getAfterSaleReasons(type)
        this.reasons = res.data || []
      } catch (error) {
        this.reasons = []
      }
    },

    async fetchAfterSaleStatistics() {
      try {
        const res = await getAfterSaleStatistics()
        this.statistics = res.data
      } catch (error) {
        this.statistics = null
      }
    },

    async fetchReturnAddresses(shopId) {
      try {
        const res = await getReturnAddresses(shopId)
        this.returnAddresses = res.data || []
      } catch (error) {
        this.returnAddresses = []
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

    clearCurrentAfterSale() {
      this.currentAfterSale = null
    }
  }
})