<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo" @click="toggleSidebar">
          <div class="logo-icon">⚡</div>
          <span class="logo-text" v-show="!sidebarCollapsed">PLN Monitor</span>
        </div>
        <button 
          class="sidebar-toggle"
          @click="toggleSidebar"
          :class="{ collapsed: sidebarCollapsed }"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>

      <nav class="sidebar-nav">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/dashboard" class="nav-link" active-class="active">
              <span class="nav-icon">📊</span>
              <span class="nav-text" v-show="!sidebarCollapsed">Dashboard</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/transformers" class="nav-link" active-class="active">
              <span class="nav-icon">⚡</span>
              <span class="nav-text" v-show="!sidebarCollapsed">Trafo</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/reports" class="nav-link" active-class="active">
              <span class="nav-icon">📈</span>
              <span class="nav-text" v-show="!sidebarCollapsed">Laporan</span>
            </router-link>
          </li>
          <li class="nav-item" v-if="authStore.isAdmin">
            <router-link to="/users" class="nav-link" active-class="active">
              <span class="nav-icon">👥</span>
              <span class="nav-text" v-show="!sidebarCollapsed">Pengguna</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/settings" class="nav-link" active-class="active">
              <span class="nav-icon">⚙️</span>
              <span class="nav-text" v-show="!sidebarCollapsed">Pengaturan</span>
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info" v-if="authStore.currentUser">
          <div class="user-avatar">
            <img 
              :src="authStore.currentUser.avatar || '/images/admin-avatar.png'" 
              :alt="authStore.currentUser.username"
            />
          </div>
          <div class="user-details" v-show="!sidebarCollapsed">
            <div class="user-name">{{ authStore.currentUser.username }}</div>
            <div class="user-role">{{ authStore.currentUser.role }}</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <!-- Top Bar -->
      <header class="top-bar">
        <div class="top-bar-left">
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        
        <div class="top-bar-right">
          <!-- Theme Toggle -->
          <button class="theme-toggle" @click="themeStore.toggleTheme()">
            <span v-if="themeStore.isDarkMode">🌙</span>
            <span v-else>☀️</span>
          </button>

          <!-- Notifications -->
          <div class="notifications-dropdown" ref="notificationsRef">
            <button 
              class="notifications-trigger" 
              @click="showNotifications = !showNotifications"
              :class="{ active: showNotifications }"
            >
              <span class="notification-icon">🔔</span>
              <span class="notification-badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
            </button>

            <div class="notifications-panel" v-show="showNotifications">
              <div class="notifications-header">
                <h3>Notifikasi</h3>
                <button @click="markAllAsRead" class="mark-read-btn">Tandai semua dibaca</button>
              </div>
              <div class="notifications-list">
                <div 
                  v-for="notification in notifications.slice(0, 5)" 
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.isRead }"
                >
                  <div class="notification-content">
                    <div class="notification-title">{{ notification.title }}</div>
                    <div class="notification-message">{{ notification.message }}</div>
                    <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
                  </div>
                </div>
                <div v-if="notifications.length === 0" class="no-notifications">
                  Tidak ada notifikasi
                </div>
              </div>
            </div>
          </div>

          <!-- User Menu -->
          <div class="user-menu-dropdown" ref="userMenuRef">
            <button 
              class="user-menu-trigger" 
              @click="showUserMenu = !showUserMenu"
              :class="{ active: showUserMenu }"
            >
              <img 
                :src="authStore.currentUser?.avatar || '/images/admin-avatar.png'" 
                :alt="authStore.currentUser?.username"
                class="user-avatar-small"
              />
            </button>

            <div class="user-menu-panel" v-show="showUserMenu">
              <div class="user-menu-header">
                <div class="user-info-detailed">
                  <div class="user-name">{{ authStore.currentUser?.username }}</div>
                  <div class="user-email">{{ authStore.currentUser?.email }}</div>
                  <div class="user-role-badge" :class="authStore.currentUser?.role">
                    {{ authStore.currentUser?.role }}
                  </div>
                </div>
              </div>
              <div class="user-menu-actions">
                <button class="user-menu-item" @click="logout">
                  <span class="menu-icon">🚪</span>
                  Keluar
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="page-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import type { Notification } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const sidebarCollapsed = ref(false)
const showNotifications = ref(false)
const showUserMenu = ref(false)
const notificationsRef = ref<HTMLElement>()
const userMenuRef = ref<HTMLElement>()

// Mock notifications data
const notifications = ref<Notification[]>([
  {
    id: 1,
    userId: 1,
    title: 'Status Trafo Berubah',
    message: 'Trafo TRF001 status berubah ke Maintenance',
    type: 'warning',
    isRead: false,
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    userId: 1,
    title: 'Trafo Selesai',
    message: 'Trafo TRF002 telah selesai diperbaiki',
    type: 'success',
    isRead: false,
    createdAt: new Date(Date.now() - 3600000).toISOString()
  }
])

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/dashboard': 'Dashboard',
    '/transformers': 'Monitoring Trafo',
    '/reports': 'Laporan & Analitik',
    '/users': 'Manajemen Pengguna',
    '/settings': 'Pengaturan'
  }
  return titles[route.path] || 'PLN Monitor'
})

const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.isRead).length
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('sidebar-collapsed', sidebarCollapsed.value.toString())
}

const markAllAsRead = () => {
  notifications.value.forEach(n => n.isRead = true)
}

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return 'Baru saja'
  if (minutes < 60) return `${minutes} menit yang lalu`
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} jam yang lalu`
  
  const days = Math.floor(hours / 24)
  return `${days} hari yang lalu`
}

const logout = async () => {
  authStore.logout()
  router.push('/login')
}

const handleClickOutside = (event: Event) => {
  if (notificationsRef.value && !notificationsRef.value.contains(event.target as Node)) {
    showNotifications.value = false
  }
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  // Load sidebar state
  const savedState = localStorage.getItem('sidebar-collapsed')
  if (savedState) {
    sidebarCollapsed.value = savedState === 'true'
  }
  
  // Add click outside listener
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary);
}

/* Sidebar Styles */
.sidebar {
  width: 280px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal);
  position: relative;
  z-index: 100;
}

.sidebar-collapsed .sidebar {
  width: 70px;
}

.sidebar-header {
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: var(--glow-primary);
  transition: all var(--transition-normal);
}

.logo-text {
  font-size: var(--font-size-xl);
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: opacity var(--transition-normal);
}

.sidebar-toggle {
  width: 30px;
  height: 30px;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  transition: all var(--transition-normal);
}

.sidebar-toggle span {
  width: 18px;
  height: 2px;
  background: var(--text-secondary);
  transition: all var(--transition-normal);
}

.sidebar-toggle.collapsed span:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.sidebar-toggle.collapsed span:nth-child(2) {
  opacity: 0;
}

.sidebar-toggle.collapsed span:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -6px);
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-4);
  overflow-y: auto;
}

.nav-list {
  list-style: none;
}

.nav-item {
  margin-bottom: var(--space-2);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.nav-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.1), transparent);
  transition: left 0.5s;
}

.nav-link:hover::before {
  left: 100%;
}

.nav-link:hover,
.nav-link.active {
  color: var(--primary-color);
  background: rgba(0, 212, 255, 0.1);
  transform: translateX(5px);
}

.nav-link.active {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(0, 255, 136, 0.1));
  box-shadow: var(--glow-primary);
}

.nav-icon {
  font-size: 20px;
  min-width: 20px;
}

.nav-text {
  font-weight: 500;
  transition: opacity var(--transition-normal);
}

.sidebar-footer {
  padding: var(--space-4);
  border-top: 1px solid var(--border-color);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--primary-color);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  min-width: 0;
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-transform: uppercase;
}

/* Main Content Styles */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.top-bar {
  height: 70px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
  position: sticky;
  top: 0;
  z-index: 90;
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.theme-toggle {
  width: 40px;
  height: 40px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all var(--transition-normal);
}

.theme-toggle:hover {
  background: var(--primary-color);
  color: white;
  box-shadow: var(--glow-primary);
}

/* Notifications Dropdown */
.notifications-dropdown {
  position: relative;
}

.notifications-trigger {
  width: 40px;
  height: 40px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all var(--transition-normal);
  position: relative;
}

.notifications-trigger:hover,
.notifications-trigger.active {
  background: var(--primary-color);
  color: white;
  box-shadow: var(--glow-primary);
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: var(--status-emergency);
  color: white;
  font-size: var(--font-size-xs);
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.notifications-panel {
  position: absolute;
  top: 100%;
  right: 0;
  width: 350px;
  max-height: 400px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  z-index: 1000;
  margin-top: var(--space-2);
  overflow: hidden;
}

.notifications-header {
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notifications-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.mark-read-btn {
  background: transparent;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: var(--font-size-xs);
  transition: color var(--transition-normal);
}

.mark-read-btn:hover {
  color: var(--accent-color);
}

.notifications-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  transition: background var(--transition-normal);
}

.notification-item:hover {
  background: var(--bg-tertiary);
}

.notification-item.unread {
  background: rgba(0, 212, 255, 0.05);
  border-left: 3px solid var(--primary-color);
}

.notification-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.notification-message {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-2);
}

.notification-time {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.no-notifications {
  padding: var(--space-8);
  text-align: center;
  color: var(--text-muted);
}

/* User Menu Dropdown */
.user-menu-dropdown {
  position: relative;
}

.user-menu-trigger {
  width: 40px;
  height: 40px;
  background: transparent;
  border: 2px solid var(--border-color);
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  transition: all var(--transition-normal);
}

.user-menu-trigger:hover,
.user-menu-trigger.active {
  border-color: var(--primary-color);
  box-shadow: var(--glow-primary);
}

.user-avatar-small {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-menu-panel {
  position: absolute;
  top: 100%;
  right: 0;
  width: 250px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  z-index: 1000;
  margin-top: var(--space-2);
  overflow: hidden;
}

.user-menu-header {
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.user-info-detailed .user-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.user-info-detailed .user-email {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-2);
}

.user-role-badge {
  display: inline-block;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  text-transform: uppercase;
}

.user-role-badge.admin {
  background: rgba(255, 68, 68, 0.2);
  color: var(--status-emergency);
}

.user-role-badge.operator {
  background: rgba(0, 212, 255, 0.2);
  color: var(--primary-color);
}

.user-role-badge.viewer {
  background: rgba(0, 255, 136, 0.2);
  color: var(--accent-color);
}

.user-menu-actions {
  padding: var(--space-2);
}

.user-menu-item {
  width: 100%;
  padding: var(--space-3);
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  transition: all var(--transition-normal);
}

.user-menu-item:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.page-content {
  flex: 1;
  padding: var(--space-6);
  overflow-y: auto;
}

/* Responsive Design */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 200;
    transform: translateX(-100%);
    transition: transform var(--transition-normal);
  }
  
  .sidebar-collapsed .sidebar {
    transform: translateX(0);
    width: 280px;
  }
  
  .main-content {
    width: 100%;
  }
  
  .top-bar {
    padding: 0 var(--space-4);
  }
  
  .page-content {
    padding: var(--space-4);
  }
}
</style>
