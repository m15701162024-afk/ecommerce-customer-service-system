import { defineStore } from 'pinia'
import { getProductList, getProductDetail, createProduct, updateProduct, deleteProduct, searchProducts } from '@/api/product'

/**
 * 商品管理 Store
 * 管理商品列表、当前商品、分页信息和搜索过滤功能
 */
export const useProductStore = defineStore('product', {
  state: () => ({
    // 商品列表
    productList: [],
    // 当前选中的商品
    currentProduct: null,
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 10,
      total: 0
    },
    // 加载状态
    loading: false,
    // 搜索关键词
    searchKeyword: '',
    // 过滤条件
    filters: {
      category: '',
      status: '',
      priceRange: []
    }
  }),

  getters: {
    // 获取商品总数
    productTotal: (state) => state.pagination.total,
    // 判断是否有商品
    hasProducts: (state) => state.productList.length > 0,
    // 获取上架商品
    activeProducts: (state) => state.productList.filter(p => p.status === 'active'),
    // 获取下架商品
    inactiveProducts: (state) => state.productList.filter(p => p.status === 'inactive')
  },

  actions: {
    /**
     * 获取商品列表
     * @param {Object} params - 查询参数
     */
    async fetchProducts(params = {}) {
      this.loading = true
      try {
        const res = await getProductList({
          page: this.pagination.page,
          pageSize: this.pagination.pageSize,
          ...params
        })
        this.productList = res.data?.list || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('获取商品列表失败:', error)
        this.productList = []
        this.pagination.total = 0
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取商品详情
     * @param {string|number} productId - 商品ID
     */
    async fetchProductDetail(productId) {
      this.loading = true
      try {
        const res = await getProductDetail(productId)
        this.currentProduct = res.data
        return res.data
      } catch (error) {
        console.error('获取商品详情失败:', error)
        this.currentProduct = null
        return null
      } finally {
        this.loading = false
      }
    },

    /**
     * 创建商品
     * @param {Object} productData - 商品数据
     */
    async createProductAction(productData) {
      this.loading = true
      try {
        const res = await createProduct(productData)
        // 重新获取列表
        await this.fetchProducts()
        return { success: true, data: res.data }
      } catch (error) {
        console.error('创建商品失败:', error)
        return { success: false, message: error.message || '创建商品失败' }
      } finally {
        this.loading = false
      }
    },

    /**
     * 更新商品
     * @param {string|number} productId - 商品ID
     * @param {Object} productData - 更新数据
     */
    async updateProductAction(productId, productData) {
      this.loading = true
      try {
        const res = await updateProduct(productId, productData)
        // 更新列表中的商品
        const index = this.productList.findIndex(p => p.id === productId)
        if (index !== -1) {
          this.productList[index] = { ...this.productList[index], ...productData }
        }
        // 如果是当前商品，也更新
        if (this.currentProduct?.id === productId) {
          this.currentProduct = { ...this.currentProduct, ...productData }
        }
        return { success: true, data: res.data }
      } catch (error) {
        console.error('更新商品失败:', error)
        return { success: false, message: error.message || '更新商品失败' }
      } finally {
        this.loading = false
      }
    },

    /**
     * 删除商品
     * @param {string|number} productId - 商品ID
     */
    async deleteProductAction(productId) {
      this.loading = true
      try {
        await deleteProduct(productId)
        // 从列表中移除
        this.productList = this.productList.filter(p => p.id !== productId)
        this.pagination.total -= 1
        // 如果是当前商品，清空
        if (this.currentProduct?.id === productId) {
          this.currentProduct = null
        }
        return { success: true }
      } catch (error) {
        console.error('删除商品失败:', error)
        return { success: false, message: error.message || '删除商品失败' }
      } finally {
        this.loading = false
      }
    },

    /**
     * 搜索商品
     * @param {string} keyword - 搜索关键词
     */
    async searchProductsAction(keyword) {
      this.loading = true
      this.searchKeyword = keyword
      try {
        const res = await searchProducts({ keyword })
        this.productList = res.data?.list || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('搜索商品失败:', error)
        this.productList = []
        this.pagination.total = 0
      } finally {
        this.loading = false
      }
    },

    /**
     * 过滤商品
     * @param {Object} filters - 过滤条件
     */
    async filterProductsAction(filters) {
      this.loading = true
      this.filters = { ...this.filters, ...filters }
      try {
        const res = await getProductList({
          page: this.pagination.page,
          pageSize: this.pagination.pageSize,
          ...this.filters
        })
        this.productList = res.data?.list || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        console.error('过滤商品失败:', error)
        this.productList = []
        this.pagination.total = 0
      } finally {
        this.loading = false
      }
    },

    /**
     * 设置当前商品
     * @param {Object} product - 商品对象
     */
    setCurrentProduct(product) {
      this.currentProduct = product
    },

    /**
     * 设置分页
     * @param {Object} pagination - 分页参数
     */
    setPagination(pagination) {
      this.pagination = { ...this.pagination, ...pagination }
    },

    /**
     * 重置过滤器
     */
    resetFilters() {
      this.filters = {
        category: '',
        status: '',
        priceRange: []
      }
      this.searchKeyword = ''
    }
  }
})