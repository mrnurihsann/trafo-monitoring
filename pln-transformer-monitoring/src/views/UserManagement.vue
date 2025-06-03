<template>
  <div class="user-management">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header-left">
        <h1>Manajemen Pengguna</h1>
        <p class="page-description">Kelola akun pengguna dan hak akses sistem</p>
      </div>
      <div class="page-header-right">
        <button class="btn btn-secondary" @click="refreshUsers">
          🔄 Refresh
        </button>
        <button class="btn btn-primary" @click="showCreateModal = true">
          ➕ Tambah Pengguna
        </button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card card">
        <div class="stat-icon admin">👑</div>
        <div class="stat-content">
          <div class="stat-number">{{ userStats.admin }}</div>
          <div class="stat-label">Admin</div>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon operator">👷</div>
        <div class="stat-content">
          <div class="stat-number">{{ userStats.operator }}</div>
          <div class="stat-label">Operator</div>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon viewer">👁️</div>
        <div class="stat-content">
          <div class="stat-number">{{ userStats.viewer }}</div>
          <div class="stat-label">Viewer</div>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon total">👥</div>
        <div class="stat-content">
          <div class="stat-number">{{ users.length }}</div>
          <div class="stat-label">Total</div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-section card">
      <div class="filters-row">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Cari pengguna..."
            class="form-input"
          />
          <span class="search-icon">🔍</span>
        </div>
        
        <select v-model="roleFilter" class="form-select">
          <option value="">Semua Role</option>
          <option value="admin">Admin</option>
          <option value="operator">Operator</option>
          <option value="viewer">Viewer</option>
        </select>

        <select v-model="sortBy" class="form-select">
          <option value="username">Username</option>
          <option value="role">Role</option>
          <option value="createdAt">Tanggal Dibuat</option>
          <option value="updatedAt">Update Terakhir</option>
        </select>
      </div>
    </div>

    <!-- Users Grid -->
    <div class="users-grid" v-if="!loading">
      <div 
        v-for="user in paginatedUsers" 
        :key="user.id"
        class="user-card card glow-effect"
      >
        <div class="user-header">
          <div class="user-avatar">
            <img 
              :src="user.avatar || '/images/admin-avatar.png'" 
              :alt="user.username"
            />
          </div>
          <div class="user-role-badge" :class="user.role">
            {{ user.role }}
          </div>
        </div>

        <div class="user-content">
          <h3 class="user-name">{{ user.username }}</h3>
          <p class="user-email">📧 {{ user.email }}</p>
          
          <div class="user-meta">
            <div class="meta-item">
              <span class="meta-label">Created:</span>
              <span class="meta-value">{{ formatDate(user.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">Updated:</span>
              <span class="meta-value">{{ formatDate(user.updatedAt) }}</span>
            </div>
          </div>

          <div class="user-stats" v-if="getUserStats(user)">
            <div class="stats-row">
              <div class="stat-item">
                <span class="stat-value">{{ getUserStats(user).assigned }}</span>
                <span class="stat-label">Assigned</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ getUserStats(user).completed }}</span>
                <span class="stat-label">Completed</span>
              </div>
            </div>
          </div>
        </div>

        <div class="user-actions">
          <button 
            class="action-btn edit-btn"
            @click="editUser(user)"
            title="Edit pengguna"
          >
            ✏️
          </button>
          <button 
            class="action-btn reset-btn"
            @click="resetPassword(user)"
            title="Reset password"
          >
            🔑
          </button>
          <button 
            v-if="user.id !== authStore.currentUser?.id"
            class="action-btn delete-btn"
            @click="deleteUser(user)"
            title="Hapus pengguna"
          >
            🗑️
          </button>
        </div>
      </div>

      <!-- No Results -->
      <div v-if="filteredUsers.length === 0" class="no-results">
        <div class="no-results-icon">👥</div>
        <h3>Tidak ada pengguna ditemukan</h3>
        <p>Coba ubah filter atau kata kunci pencarian</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Memuat data pengguna...</p>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        class="btn btn-ghost"
        @click="currentPage = Math.max(1, currentPage - 1)"
        :disabled="currentPage === 1"
      >
        ← Sebelumnya
      </button>
      
      <div class="page-numbers">
        <button
          v-for="page in visiblePages"
          :key="page"
          @click="currentPage = page"
          class="page-btn"
          :class="{ active: page === currentPage }"
        >
          {{ page }}
        </button>
      </div>
      
      <button 
        class="btn btn-ghost"
        @click="currentPage = Math.min(totalPages, currentPage + 1)"
        :disabled="currentPage === totalPages"
      >
        Selanjutnya →
      </button>
    </div>

    <!-- User Modal -->
    <UserModal
      v-if="showCreateModal || showEditModal"
      :user="selectedUser"
      :is-edit="showEditModal"
      @close="closeModals"
      @save="handleSave"
    />

    <!-- Password Reset Modal -->
    <PasswordResetModal
      v-if="showPasswordModal"
      :user="selectedUser"
      @close="showPasswordModal = false"
      @reset="handlePasswordReset"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useTransformerStore } from '@/stores/transformers'
import { api } from '@/services/api'
import type { User } from '@/types'
import UserModal from '@/components/UserModal.vue'
import PasswordResetModal from '@/components/PasswordResetModal.vue'

const authStore = useAuthStore()
const transformerStore = useTransformerStore()

const loading = ref(false)
const users = ref<User[]>([])
const searchQuery = ref('')
const roleFilter = ref('')
const sortBy = ref('username')
const currentPage = ref(1)
const itemsPerPage = 12

// Modal states
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showPasswordModal = ref(false)
const selectedUser = ref<User | null>(null)

// Computed properties
const userStats = computed(() => {
  const stats = { admin: 0, operator: 0, viewer: 0 }
  users.value.forEach(user => {
    stats[user.role]++
  })
  return stats
})

const filteredUsers = computed(() => {
  let filtered = [...users.value]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(user => 
      user.username.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    )
  }

  // Role filter
  if (roleFilter.value) {
    filtered = filtered.filter(user => user.role === roleFilter.value)
  }

  // Sorting
  filtered.sort((a, b) => {
    const aValue = a[sortBy.value as keyof User]
    const bValue = b[sortBy.value as keyof User]
    
    if (typeof aValue === 'string' && typeof bValue === 'string') {
      return aValue.localeCompare(bValue)
    }
    return 0
  })

  return filtered
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredUsers.value.slice(start, end)
})

const totalPages = computed(() => 
  Math.ceil(filteredUsers.value.length / itemsPerPage)
)

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// Methods
const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('id-ID', {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit'
  })
}

const getUserStats = (user: User) => {
  if (user.role === 'viewer') return null
  
  const assigned = transformerStore.transformers.filter(t => t.assignedOperatorId === user.id).length
  const completed = transformerStore.transformers.filter(t => 
    t.assignedOperatorId === user.id && t.currentStatus === 'completed'
  ).length
  
  return { assigned, completed }
}

const editUser = (user: User) => {
  selectedUser.value = user
  showEditModal.value = true
}

const resetPassword = (user: User) => {
  selectedUser.value = user
  showPasswordModal.value = true
}

const deleteUser = async (user: User) => {
  if (!confirm(`Hapus pengguna ${user.username}?`)) return
  
  try {
    // Remove user from users array (mock deletion)
    const index = users.value.findIndex(u => u.id === user.id)
    if (index !== -1) {
      users.value.splice(index, 1)
    }
    
    // Show success notification
    console.log('User deleted successfully')
  } catch (error) {
    console.error('Error deleting user:', error)
    // Show error notification
  }
}

const refreshUsers = async () => {
  loading.value = true
  try {
    const data = await api.getUsers()
    users.value = data
  } catch (error) {
    console.error('Error loading users:', error)
  } finally {
    loading.value = false
  }
}

const closeModals = () => {
  showCreateModal.value = false
  showEditModal.value = false
  selectedUser.value = null
}

const handleSave = async (userData: any) => {
  try {
    if (showEditModal.value && selectedUser.value) {
      // Update existing user
      const index = users.value.findIndex(u => u.id === selectedUser.value!.id)
      if (index !== -1) {
        users.value[index] = {
          ...users.value[index],
          ...userData,
          updatedAt: new Date().toISOString()
        }
      }
    } else {
      // Create new user
      const newUser: User = {
        id: Date.now(),
        ...userData,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      users.value.push(newUser)
    }
    
    closeModals()
    // Show success notification
  } catch (error) {
    console.error('Error saving user:', error)
    // Show error notification
  }
}

const handlePasswordReset = async (newPassword: string) => {
  try {
    // Mock password reset
    console.log('Password reset for user:', selectedUser.value?.username)
    showPasswordModal.value = false
    selectedUser.value = null
    // Show success notification
  } catch (error) {
    console.error('Error resetting password:', error)
    // Show error notification
  }
}

// Watch for filter changes to reset pagination
watch([searchQuery, roleFilter], () => {
  currentPage.value = 1
})

onMounted(async () => {
  await refreshUsers()
})
</script>

<style scoped>
.user-management {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-8);
}

.page-header-left h1 {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
}

.page-description {
  color: var(--text-secondary);
  margin: 0;
}

.page-header-right {
  display: flex;
  gap: var(--space-3);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-icon.admin { background: linear-gradient(135deg, #ef4444, #dc2626); }
.stat-icon.operator { background: linear-gradient(135deg, #00D4FF, #0099cc); }
.stat-icon.viewer { background: linear-gradient(135deg, #00FF88, #00cc66); }
.stat-icon.total { background: linear-gradient(135deg, #8B5FBF, #6d4c99); }

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: var(--font-size-2xl);
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: var(--space-1);
}

.stat-label {
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  font-size: var(--font-size-sm);
}

.filters-section {
  padding: var(--space-6);
  margin-bottom: var(--space-8);
}

.filters-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: var(--space-4);
}

.search-box {
  position: relative;
}

.search-box .form-input {
  padding-left: var(--space-10);
}

.search-icon {
  position: absolute;
  left: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  font-size: 16px;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.user-card {
  position: relative;
  padding: var(--space-6);
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.user-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-xl);
  border-color: var(--primary-color);
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--primary-color);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-role-badge {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
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

.user-content {
  margin-bottom: var(--space-4);
}

.user-name {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.user-email {
  color: var(--text-secondary);
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-sm);
}

.user-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.meta-label {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.meta-value {
  color: var(--text-primary);
  font-weight: 500;
  font-size: var(--font-size-sm);
}

.user-stats {
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  border: 1px solid var(--border-color);
}

.stats-row {
  display: flex;
  gap: var(--space-4);
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-weight: 700;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  text-transform: uppercase;
}

.user-actions {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  display: flex;
  gap: var(--space-2);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.user-card:hover .user-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all var(--transition-fast);
}

.edit-btn {
  background: rgba(255, 215, 0, 0.2);
  color: var(--status-maintenance);
}

.reset-btn {
  background: rgba(0, 212, 255, 0.2);
  color: var(--primary-color);
}

.delete-btn {
  background: rgba(255, 68, 68, 0.2);
  color: var(--status-emergency);
}

.action-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.no-results {
  grid-column: 1 / -1;
  text-align: center;
  padding: var(--space-16);
  color: var(--text-muted);
}

.no-results-icon {
  font-size: 64px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.no-results h3 {
  margin-bottom: var(--space-2);
  color: var(--text-secondary);
}

.loading-state {
  text-align: center;
  padding: var(--space-16);
}

.loading-state p {
  color: var(--text-muted);
  margin-top: var(--space-4);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-8);
}

.page-numbers {
  display: flex;
  gap: var(--space-1);
}

.page-btn {
  width: 40px;
  height: 40px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  transition: all var(--transition-normal);
}

.page-btn:hover,
.page-btn.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--glow-primary);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .filters-row {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-3);
  }
  
  .users-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: var(--space-4);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--space-4);
    align-items: stretch;
  }
  
  .page-header-right {
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: var(--space-4);
  }
  
  .filters-row {
    grid-template-columns: 1fr;
  }
  
  .users-grid {
    grid-template-columns: 1fr;
  }
  
  .user-card {
    padding: var(--space-4);
  }
  
  .user-actions {
    position: static;
    opacity: 1;
    justify-content: center;
    margin-top: var(--space-4);
  }
}
</style>
