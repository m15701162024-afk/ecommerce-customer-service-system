import { defineStore } from 'pinia'
import { getOrderList, getOrderDetail } from '@/api/order'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orderList: [],
    currentOrder: null,
    total: 0,
    loading: false
  }),

  actions: {
    async fetchOrderList(params) {
      this.loading = true
      try {
        const res = await getOrderList(params)
        this.orderList = res.data?.list || []
        this.total = res.data?.total || 0
      } catch (error) {
        this.orderList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    async fetchOrderDetail(orderId) {
      this.loading = true
      try {
        const res = await getOrderDetail(orderId)
        this.currentOrder = res.data
      } catch (error) {
        this.currentOrder = null
      } finally {
        this.loading = false
      }
    }
  }
})