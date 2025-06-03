import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import TransformerList from '@/views/TransformerList.vue'
import TransformerDetail from '@/views/TransformerDetail.vue'
import Reports from '@/views/Reports.vue'
import UserManagement from '@/views/UserManagement.vue'
import Settings from '@/views/Settings.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { requiresGuest: true }
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: Dashboard,
      meta: { requiresAuth: true }
    },
    {
      path: '/transformers',
      name: 'TransformerList',
      component: TransformerList,
      meta: { requiresAuth: true }
    },
    {
      path: '/transformers/:id',
      name: 'TransformerDetail',
      component: TransformerDetail,
      meta: { requiresAuth: true }
    },
    {
      path: '/reports',
      name: 'Reports',
      component: Reports,
      meta: { requiresAuth: true }
    },
    {
      path: '/users',
      name: 'UserManagement',
      component: UserManagement,
      meta: { requiresAuth: true, roles: ['admin'] }
    },
    {
      path: '/settings',
      name: 'Settings',
      component: Settings,
      meta: { requiresAuth: true }
    }
  ]
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next('/dashboard')
  } else if (to.meta.roles && !to.meta.roles.includes(authStore.currentUser?.role)) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
