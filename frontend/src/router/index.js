import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/generate',
      name: 'Generate',
      component: () => import('@/views/Generate.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/script/:sessionId',
      name: 'ScriptList',
      component: () => import('@/views/ScriptList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/script/detail/:versionId',
      name: 'ScriptDetail',
      component: () => import('@/views/ScriptDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/history',
      name: 'History',
      component: () => import('@/views/History.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 需要认证但未登录，跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    // 已登录用户访问登录/注册页，跳转到首页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router

