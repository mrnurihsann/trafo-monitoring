import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { api } from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!currentUser.value && !!token.value)
  const isAdmin = computed(() => currentUser.value?.role === 'admin')
  const isOperator = computed(() => currentUser.value?.role === 'operator')
  const isViewer = computed(() => currentUser.value?.role === 'viewer')

  const login = async (username: string, password: string) => {
    loading.value = true
    try {
      // Mock login for demo purposes
      const response = await fetch('/data/users.json')
      const usersData = await response.json()
      const user = usersData.find((u: any) => u.username === username)
      
      if (user && password === 'password123') {
        // Convert data format to match User interface
        const formattedUser: User = {
          id: user.id,
          username: user.username,
          email: user.email,
          role: user.role as 'admin' | 'operator' | 'viewer',
          avatar: user.avatar,
          createdAt: user.created_at || new Date().toISOString(),
          updatedAt: user.last_login || new Date().toISOString()
        }
        
        currentUser.value = formattedUser
        token.value = 'mock-jwt-token-' + user.id
        localStorage.setItem('auth-token', token.value)
        localStorage.setItem('current-user', JSON.stringify(formattedUser))
        return { success: true }
      } else {
        throw new Error('Username atau password salah')
      }
    } catch (error) {
      console.error('Login error:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    currentUser.value = null
    token.value = null
    localStorage.removeItem('auth-token')
    localStorage.removeItem('current-user')
  }

  const initAuth = () => {
    const savedToken = localStorage.getItem('auth-token')
    const savedUser = localStorage.getItem('current-user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      currentUser.value = JSON.parse(savedUser)
    }
  }

  const updateUser = (userData: Partial<User>) => {
    if (currentUser.value) {
      currentUser.value = { ...currentUser.value, ...userData }
      localStorage.setItem('current-user', JSON.stringify(currentUser.value))
    }
  }

  return {
    currentUser,
    token,
    loading,
    isAuthenticated,
    isAdmin,
    isOperator,
    isViewer,
    login,
    logout,
    initAuth,
    updateUser
  }
})
