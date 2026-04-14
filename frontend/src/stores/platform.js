import { defineStore } from 'pinia'
import {
  getPlatformList,
  connectPlatform,
  updatePlatformConfig,
  syncPlatformData
} from '@/api/platform'

export const usePlatformStore = defineStore('platform', {
  state: () => ({
    platformList: [],
    currentPlatform: null,
    loading: false,
    connectionStatus: {},
    syncProgress: {}
  }),

  getters: {
    hasPlatforms: (state) => state.platformList.length > 0,
    connectedPlatforms: (state) => state.platformList.filter(p => state.connectionStatus[p.id] === 'connected'),
    disconnectedPlatforms: (state) => state.platformList.filter(p => state.connectionStatus[p.id] !== 'connected')
  },

  actions: {
    async fetchPlatformList() {
      this.loading = true
      try {
        const res = await getPlatformList()
        this.platformList = res.data || []
        this.platformList.forEach(platform => {
          this.connectionStatus[platform.id] = platform.status || 'disconnected'
        })
      } catch (error) {
        console.error('获取平台列表失败:', error)
        this.platformList = []
      } finally {
        this.loading = false
      }
    },

    async connectPlatformAction(platformId, connectData) {
      this.loading = true
      try {
        const res = await connectPlatform(platformId, connectData)
        this.connectionStatus[platformId] = 'connected'
        const index = this.platformList.findIndex(p => p.id === platformId)
        if (index !== -1) {
          this.platformList[index].status = 'connected'
        }
        return { success: true, data: res.data }
      } catch (error) {
        console.error('连接平台失败:', error)
        this.connectionStatus[platformId] = 'disconnected'
        return { success: false, message: error.message || '连接平台失败' }
      } finally {
        this.loading = false
      }
    },

    async updateConfigAction(platformId, configData) {
      this.loading = true
      try {
        const res = await updatePlatformConfig(platformId, configData)
        const index = this.platformList.findIndex(p => p.id === platformId)
        if (index !== -1) {
          this.platformList[index].config = { ...this.platformList[index].config, ...configData }
        }
        if (this.currentPlatform?.id === platformId) {
          this.currentPlatform.config = { ...this.currentPlatform.config, ...configData }
        }
        return { success: true, data: res.data }
      } catch (error) {
        console.error('更新平台配置失败:', error)
        return { success: false, message: error.message || '更新平台配置失败' }
      } finally {
        this.loading = false
      }
    },

    async syncPlatformDataAction(platformId) {
      this.loading = true
      this.syncProgress[platformId] = 'syncing'
      try {
        const res = await syncPlatformData(platformId)
        this.syncProgress[platformId] = 'completed'
        return { success: true, data: res.data }
      } catch (error) {
        console.error('同步平台数据失败:', error)
        this.syncProgress[platformId] = 'failed'
        return { success: false, message: error.message || '同步平台数据失败' }
      } finally {
        this.loading = false
      }
    },

    setCurrentPlatform(platform) {
      this.currentPlatform = platform
    },

    clearCurrentPlatform() {
      this.currentPlatform = null
    }
  }
})