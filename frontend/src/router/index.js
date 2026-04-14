import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '控制台', icon: 'Odometer', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/orders/index.vue'),
        meta: { title: '订单管理', icon: 'List', requiresAuth: true }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/products/index.vue'),
        meta: { title: '商品管理', icon: 'Goods', requiresAuth: true }
      },
      {
        path: 'purchase',
        name: 'Purchase',
        component: () => import('@/views/purchase/index.vue'),
        meta: { title: '采购管理', icon: 'ShoppingCart', requiresAuth: true }
      },
      {
        path: 'orders/detail/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/orders/Detail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      {
        path: 'purchase/detail/:orderNo',
        name: 'PurchaseDetail',
        component: () => import('@/views/purchase/Detail.vue'),
        meta: { title: '采购详情', requiresAuth: true }
      },
      {
        path: 'customer-service',
        name: 'CustomerService',
        component: () => import('@/views/customer-service/index.vue'),
        meta: { title: '客服中心', icon: 'Service', requiresAuth: true }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/knowledge/index.vue'),
        meta: { title: '知识库', icon: 'Collection', requiresAuth: true }
      },
      {
        path: 'platforms',
        name: 'Platforms',
        component: () => import('@/views/platforms/index.vue'),
        meta: { title: '平台对接', icon: 'Connection', requiresAuth: true }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/index.vue'),
        meta: { title: '系统监控', icon: 'Monitor', requiresAuth: true }
      },
      {
        path: 'work-order',
        name: 'WorkOrder',
        component: () => import('@/views/work-order/index.vue'),
        meta: { title: '工单管理', icon: 'Ticket', requiresAuth: true }
      },
      {
        path: 'after-sale',
        name: 'AfterSale',
        component: () => import('@/views/after-sale/index.vue'),
        meta: { title: '售后管理', icon: 'RefreshLeft', requiresAuth: true }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/customer/index.vue'),
        meta: { title: '客户管理', icon: 'User', requiresAuth: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        redirect: '/settings/account-binding',
        meta: { title: '系统设置', icon: 'Setting', requiresAuth: true },
        children: [
          {
            path: 'account-binding',
            name: 'AccountBinding',
            component: () => import('@/views/settings/AccountBinding.vue'),
            meta: { title: '账号绑定', requiresAuth: true }
          }
        ]
      }
    ]
  },
  {
    path: '/oauth/callback/:platform',
    name: 'OAuthCallback',
    component: () => import('@/views/oauth/Callback.vue'),
    meta: { title: 'OAuth授权回调', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  if (requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    next({ path: '/dashboard' })
  } else {
    next()
  }
})

export default router