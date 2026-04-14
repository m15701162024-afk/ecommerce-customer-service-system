import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'
import { useRouter } from 'vue-router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    roles: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '未登录'
  },

  actions: {
    async loginAction(loginForm) {
      try {
        const res = await login(loginForm)
        console.log('Login response:', res)
        if (res?.token) {
          this.token = res.token
          localStorage.setItem('token', res.token)
          this.userInfo = res.user
          return { success: true }
        }
        return { success: false, message: '登录失败，请检查用户名和密码' }
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },

    async getUserInfoAction() {
      try {
        const res = await getUserInfo()
        this.userInfo = res
        this.roles = res.roles || []
      } catch (error) {
        this.logoutAction()
      }
    },

    async logoutAction() {
      try { await logout() } catch (e) {}
      this.token = ''
      this.userInfo = null
      this.roles = []
      localStorage.removeItem('token')
      const router = useRouter()
      router.push('/login')
    }
  }
})