import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ThemeConfig } from '@/types'

export const useThemeStore = defineStore('theme', () => {
  const isDarkMode = ref(false)
  const primaryColor = ref('#00D4FF')
  const accentColor = ref('#00FF88')

  const themeConfig = computed((): ThemeConfig => ({
    isDark: isDarkMode.value,
    primaryColor: primaryColor.value,
    accentColor: accentColor.value
  }))

  const toggleTheme = () => {
    isDarkMode.value = !isDarkMode.value
    saveThemePreference()
    applyTheme()
  }

  const setTheme = (dark: boolean) => {
    isDarkMode.value = dark
    saveThemePreference()
    applyTheme()
  }

  const setPrimaryColor = (color: string) => {
    primaryColor.value = color
    saveThemePreference()
    applyTheme()
  }

  const setAccentColor = (color: string) => {
    accentColor.value = color
    saveThemePreference()
    applyTheme()
  }

  const saveThemePreference = () => {
    localStorage.setItem('theme-config', JSON.stringify(themeConfig.value))
  }

  const loadThemePreference = () => {
    const saved = localStorage.getItem('theme-config')
    if (saved) {
      const config = JSON.parse(saved) as ThemeConfig
      isDarkMode.value = config.isDark
      primaryColor.value = config.primaryColor
      accentColor.value = config.accentColor
    }
    applyTheme()
  }

  const applyTheme = () => {
    const root = document.documentElement
    
    // Apply theme class
    document.body.classList.toggle('dark-theme', isDarkMode.value)
    
    // Apply CSS custom properties
    root.style.setProperty('--primary-color', primaryColor.value)
    root.style.setProperty('--accent-color', accentColor.value)
    
    // Generate color variations
    const primaryRGB = hexToRgb(primaryColor.value)
    const accentRGB = hexToRgb(accentColor.value)
    
    if (primaryRGB) {
      root.style.setProperty('--primary-rgb', `${primaryRGB.r}, ${primaryRGB.g}, ${primaryRGB.b}`)
      root.style.setProperty('--primary-light', `rgba(${primaryRGB.r}, ${primaryRGB.g}, ${primaryRGB.b}, 0.1)`)
      root.style.setProperty('--primary-medium', `rgba(${primaryRGB.r}, ${primaryRGB.g}, ${primaryRGB.b}, 0.3)`)
    }
    
    if (accentRGB) {
      root.style.setProperty('--accent-rgb', `${accentRGB.r}, ${accentRGB.g}, ${accentRGB.b}`)
      root.style.setProperty('--accent-light', `rgba(${accentRGB.r}, ${accentRGB.g}, ${accentRGB.b}, 0.1)`)
      root.style.setProperty('--accent-medium', `rgba(${accentRGB.r}, ${accentRGB.g}, ${accentRGB.b}, 0.3)`)
    }
  }

  const hexToRgb = (hex: string) => {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    return result ? {
      r: parseInt(result[1], 16),
      g: parseInt(result[2], 16),
      b: parseInt(result[3], 16)
    } : null
  }

  const getStatusColor = (status: string): string => {
    const colors: Record<string, string> = {
      'pending': '#FFA500',
      'in-progress': '#00D4FF',
      'testing': '#8B5FBF',
      'completed': '#00FF88',
      'maintenance': '#FFD700',
      'emergency': '#FF4444',
      'offline': '#666666'
    }
    return colors[status] || '#666666'
  }

  const getPriorityColor = (priority: number): string => {
    const colors = ['#00FF88', '#FFD700', '#FFA500', '#FF6B6B', '#FF4444']
    return colors[priority - 1] || '#666666'
  }

  return {
    isDarkMode,
    primaryColor,
    accentColor,
    themeConfig,
    toggleTheme,
    setTheme,
    setPrimaryColor,
    setAccentColor,
    loadThemePreference,
    applyTheme,
    getStatusColor,
    getPriorityColor
  }
})
