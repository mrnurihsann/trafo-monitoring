import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Transformer, TransformerStatus, StatusHistory, DashboardStats } from '@/types'
import { api } from '@/services/api'
import { useAuthStore } from './auth'

export const useTransformerStore = defineStore('transformers', () => {
  const transformers = ref<Transformer[]>([])
  const statusHistory = ref<StatusHistory[]>([])
  const loading = ref(false)
  const selectedTransformer = ref<Transformer | null>(null)

  const authStore = useAuthStore()

  // Computed properties
  const totalTransformers = computed(() => transformers.value.length)
  
  const transformersByStatus = computed(() => {
    const statusCount: Record<TransformerStatus, number> = {
      'pending': 0,
      'in-progress': 0,
      'testing': 0,
      'completed': 0,
      'maintenance': 0,
      'emergency': 0,
      'offline': 0
    }
    
    transformers.value.forEach(t => {
      statusCount[t.currentStatus]++
    })
    
    return statusCount
  })

  const dashboardStats = computed((): DashboardStats => {
    const today = new Date().toISOString().split('T')[0]
    const completedToday = transformers.value.filter(t => 
      t.currentStatus === 'completed' && 
      t.updatedAt.startsWith(today)
    ).length

    return {
      totalTransformers: totalTransformers.value,
      activeTransformers: transformersByStatus.value['in-progress'] + transformersByStatus.value['testing'],
      maintenanceTransformers: transformersByStatus.value['maintenance'],
      emergencyTransformers: transformersByStatus.value['emergency'],
      completedToday,
      pendingWork: transformersByStatus.value['pending']
    }
  })

  const myAssignedTransformers = computed(() => {
    if (!authStore.currentUser) return []
    return transformers.value.filter(t => t.assignedOperatorId === authStore.currentUser?.id)
  })

  // Actions
  const fetchTransformers = async () => {
    loading.value = true
    try {
      const data = await api.getTransformers()
      transformers.value = data
    } catch (error) {
      console.error('Error fetching transformers:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchStatusHistory = async () => {
    try {
      const data = await api.getStatusHistory()
      statusHistory.value = data
    } catch (error) {
      console.error('Error fetching status history:', error)
      throw error
    }
  }

  const updateTransformerStatus = async (id: number, newStatus: TransformerStatus, reason?: string) => {
    try {
      const transformer = transformers.value.find(t => t.id === id)
      if (!transformer) throw new Error('Transformer not found')

      const previousStatus = transformer.currentStatus
      transformer.currentStatus = newStatus
      transformer.updatedAt = new Date().toISOString()

      // Update progress based on status
      switch (newStatus) {
        case 'pending':
          transformer.progress = 0
          break
        case 'in-progress':
          transformer.progress = 25
          break
        case 'testing':
          transformer.progress = 75
          break
        case 'completed':
          transformer.progress = 100
          break
        case 'maintenance':
          transformer.progress = 50
          break
      }

      // Add to status history
      const historyEntry: StatusHistory = {
        id: Date.now(),
        transformerId: id,
        previousStatus,
        newStatus,
        changedBy: authStore.currentUser?.id || 0,
        changeReason: reason,
        changedAt: new Date().toISOString(),
        user: authStore.currentUser || undefined
      }
      statusHistory.value.unshift(historyEntry)

      return transformer
    } catch (error) {
      console.error('Error updating transformer status:', error)
      throw error
    }
  }

  const createTransformer = async (transformerData: Omit<Transformer, 'id' | 'createdAt' | 'updatedAt'>) => {
    try {
      const newTransformer: Transformer = {
        ...transformerData,
        id: Date.now(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      
      transformers.value.push(newTransformer)
      return newTransformer
    } catch (error) {
      console.error('Error creating transformer:', error)
      throw error
    }
  }

  const updateTransformer = async (id: number, updates: Partial<Transformer>) => {
    try {
      const index = transformers.value.findIndex(t => t.id === id)
      if (index === -1) throw new Error('Transformer not found')

      transformers.value[index] = {
        ...transformers.value[index],
        ...updates,
        updatedAt: new Date().toISOString()
      }

      return transformers.value[index]
    } catch (error) {
      console.error('Error updating transformer:', error)
      throw error
    }
  }

  const deleteTransformer = async (id: number) => {
    try {
      const index = transformers.value.findIndex(t => t.id === id)
      if (index === -1) throw new Error('Transformer not found')

      transformers.value.splice(index, 1)
      
      // Remove related status history
      statusHistory.value = statusHistory.value.filter(h => h.transformerId !== id)
    } catch (error) {
      console.error('Error deleting transformer:', error)
      throw error
    }
  }

  const setSelectedTransformer = (transformer: Transformer | null) => {
    selectedTransformer.value = transformer
  }

  const getTransformerById = (id: number): Transformer | undefined => {
    return transformers.value.find(t => t.id === id)
  }

  const getStatusHistoryForTransformer = (transformerId: number): StatusHistory[] => {
    return statusHistory.value.filter(h => h.transformerId === transformerId)
  }

  return {
    transformers,
    statusHistory,
    loading,
    selectedTransformer,
    totalTransformers,
    transformersByStatus,
    dashboardStats,
    myAssignedTransformers,
    fetchTransformers,
    fetchStatusHistory,
    updateTransformerStatus,
    createTransformer,
    updateTransformer,
    deleteTransformer,
    setSelectedTransformer,
    getTransformerById,
    getStatusHistoryForTransformer
  }
})
