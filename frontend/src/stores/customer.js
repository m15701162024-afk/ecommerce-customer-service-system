import { defineStore } from 'pinia'
import {
  getCustomers,
  getCustomer,
  createCustomer,
  updateCustomer,
  deleteCustomer,
  searchCustomers,
  getCustomersByLevel,
  getCustomersByActivity,
  getHighValueCustomers,
  getCustomerStatistics,
  getTags,
  createTag,
  updateTag,
  deleteTag,
  getGroups,
  createGroup,
  updateGroup,
  deleteGroup
} from '@/api/customer'

export const useCustomerStore = defineStore('customer', {
  state: () => ({
    customerList: [],
    currentCustomer: null,
    total: 0,
    loading: false,
    // 分页信息
    pagination: {
      pageNum: 1,
      pageSize: 10
    },
    // 搜索条件
    searchParams: {},
    // 标签列表
    tagList: [],
    // 分组列表
    groupList: [],
    // 统计数据
    statistics: null
  }),

  getters: {
    // 是否还有更多数据
    hasMore: (state) => state.customerList.length < state.total,
    // 当前页码
    currentPage: (state) => state.pagination.pageNum,
    // 当前页面大小
    pageSize: (state) => state.pagination.pageSize,
    // 是否有选中客户
    hasCurrentCustomer: (state) => !!state.currentCustomer
  },

  actions: {
    // 获取客户列表
    async fetchCustomerList(params = {}) {
      this.loading = true
      try {
        const queryParams = {
          ...this.pagination,
          ...this.searchParams,
          ...params
        }
        const res = await getCustomers(queryParams)
        this.customerList = res.data?.list || []
        this.total = res.data?.total || 0
        if (params.pageNum) {
          this.pagination.pageNum = params.pageNum
        }
        if (params.pageSize) {
          this.pagination.pageSize = params.pageSize
        }
      } catch (error) {
        this.customerList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 获取客户详情
    async fetchCustomerDetail(id) {
      this.loading = true
      try {
        const res = await getCustomer(id)
        this.currentCustomer = res.data
      } catch (error) {
        this.currentCustomer = null
      } finally {
        this.loading = false
      }
    },

    // 创建客户
    async createCustomerAction(data) {
      this.loading = true
      try {
        const res = await createCustomer(data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    // 更新客户
    async updateCustomerAction(id, data) {
      this.loading = true
      try {
        const res = await updateCustomer(id, data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    // 删除客户
    async deleteCustomerAction(id) {
      this.loading = true
      try {
        await deleteCustomer(id)
        this.customerList = this.customerList.filter(item => item.id !== id)
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      } finally {
        this.loading = false
      }
    },

    // 搜索客户
    async searchCustomersAction(params = {}) {
      this.loading = true
      try {
        const res = await searchCustomers(params)
        this.customerList = res.data?.list || []
        this.total = res.data?.total || 0
      } catch (error) {
        this.customerList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 按等级获取客户
    async fetchCustomersByLevel(level, params = {}) {
      this.loading = true
      try {
        const res = await getCustomersByLevel(level, params)
        this.customerList = res.data?.list || []
        this.total = res.data?.total || 0
      } catch (error) {
        this.customerList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 获取高价值客户
    async fetchHighValueCustomers(params = {}) {
      this.loading = true
      try {
        const res = await getHighValueCustomers(params)
        this.customerList = res.data?.list || []
        this.total = res.data?.total || 0
      } catch (error) {
        this.customerList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 获取客户统计
    async fetchCustomerStatistics() {
      try {
        const res = await getCustomerStatistics()
        this.statistics = res.data
      } catch (error) {
        this.statistics = null
      }
    },

    // 获取标签列表
    async fetchTags(params = {}) {
      try {
        const res = await getTags(params)
        this.tagList = res.data?.list || []
      } catch (error) {
        this.tagList = []
      }
    },

    // 创建标签
    async createTagAction(data) {
      try {
        const res = await createTag(data)
        this.tagList.push(res.data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 更新标签
    async updateTagAction(id, data) {
      try {
        const res = await updateTag(id, data)
        const index = this.tagList.findIndex(item => item.id === id)
        if (index !== -1) {
          this.tagList[index] = res.data
        }
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 删除标签
    async deleteTagAction(id) {
      try {
        await deleteTag(id)
        this.tagList = this.tagList.filter(item => item.id !== id)
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 获取分组列表
    async fetchGroups(params = {}) {
      try {
        const res = await getGroups(params)
        this.groupList = res.data?.list || []
      } catch (error) {
        this.groupList = []
      }
    },

    // 创建分组
    async createGroupAction(data) {
      try {
        const res = await createGroup(data)
        this.groupList.push(res.data)
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 更新分组
    async updateGroupAction(id, data) {
      try {
        const res = await updateGroup(id, data)
        const index = this.groupList.findIndex(item => item.id === id)
        if (index !== -1) {
          this.groupList[index] = res.data
        }
        return { success: true, data: res.data }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 删除分组
    async deleteGroupAction(id) {
      try {
        await deleteGroup(id)
        this.groupList = this.groupList.filter(item => item.id !== id)
        return { success: true }
      } catch (error) {
        return { success: false, message: error.message }
      }
    },

    // 设置搜索条件
    setSearchParams(params) {
      this.searchParams = params
      this.pagination.pageNum = 1
    },

    // 重置搜索条件
    resetSearchParams() {
      this.searchParams = {}
      this.pagination.pageNum = 1
    },

    // 设置分页
    setPagination(pageNum, pageSize) {
      this.pagination.pageNum = pageNum
      this.pagination.pageSize = pageSize
    },

    // 清空当前客户
    clearCurrentCustomer() {
      this.currentCustomer = null
    }
  }
})