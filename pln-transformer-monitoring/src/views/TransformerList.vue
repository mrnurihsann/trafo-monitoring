<template>
  <div class="transformer-list">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header-left">
        <h1>Monitoring Trafo</h1>
        <p class="page-description">Kelola dan pantau status transformator PLN</p>
      </div>
      <div class="page-header-right">
        <button class="btn btn-secondary" @click="refreshData">
          🔄 Refresh
        </button>
        <button 
          v-if="!authStore.isViewer" 
          class="btn btn-primary"
          @click="showCreateModal = true"
        >
          ➕ Tambah Trafo
        </button>
      </div>
    </div>

    <!-- Filters & Search -->
    <div class="filters-section card">
      <div class="filters-grid">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Cari trafo..."
            class="form-input"
          />
          <span class="search-icon">🔍</span>
        </div>

        <select v-model="statusFilter" class="form-select">
          <option value="">Semua Status</option>
          <option value="pending">Menunggu</option>
          <option value="in-progress">Dikerjakan</option>
          <option value="testing">Testing</option>
          <option value="completed">Selesai</option>
          <option value="maintenance">Maintenance</option>
          <option value="emergency">Emergency</option>
          <option value="offline">Offline</option>
        </select>

        <select v-model="priorityFilter" class="form-select">
          <option value="">Semua Prioritas</option>
          <option value="1">Rendah</option>
          <option value="2">Normal</option>
          <option value="3">Tinggi</option>
          <option value="4">Kritis</option>
          <option value="5">Emergency</option>
        </select>

        <select v-model="sortBy" class="form-select">
          <option value="name">Nama</option>
          <option value="status">Status</option>
          <option value="priority">Prioritas</option>
          <option value="updatedAt">Terakhir Update</option>
        </select>
      </div>

      <div class="filter-stats">
        <span class="stat-item">
          Total: <strong>{{ filteredTransformers.length }}</strong>
        </span>
        <span class="stat-item">
          Aktif: <strong>{{ activeCount }}</strong>
        </span>
        <span class="stat-item">
          Emergency: <strong>{{ emergencyCount }}</strong>
        </span>
      </div>
    </div>

    <!-- Transformers Grid -->
    <div class="transformers-grid" v-if="!loading">
      <div 
        v-for="transformer in paginatedTransformers" 
        :key="transformer.id"
        class="transformer-card card glow-effect"
        @click="viewTransformer(transformer.id)"
      >
        <div class="transformer-header">
          <div class="transformer-id">{{ transformer.transformerCode }}</div>
          <div class="transformer-priority">
            <span 
              class="priority-indicator" 
              :style="{ backgroundColor: themeStore.getPriorityColor(transformer.priorityLevel) }"
            ></span>
          </div>
        </div>

        <div class="transformer-content">
          <h3 class="transformer-name">{{ transformer.name }}</h3>
          <p class="transformer-location">📍 {{ transformer.locationName }}</p>
          
          <div class="transformer-specs">
            <div class="spec-item">
              <span class="spec-label">Kapasitas:</span>
              <span class="spec-value">{{ transformer.capacityKva }} kVA</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">Tegangan:</span>
              <span class="spec-value">{{ transformer.primaryVoltage }}/{{ transformer.secondaryVoltage }} kV</span>
            </div>
          </div>

          <div class="transformer-status-section">
            <div class="status-row">
              <span class="status-badge" :class="`status-${transformer.currentStatus}`">
                {{ getStatusText(transformer.currentStatus) }}
              </span>
              <span class="progress-text">{{ transformer.progress }}%</span>
            </div>
            <div class="progress-bar" :style="{ '--progress': transformer.progress + '%' }"></div>
          </div>

          <div class="transformer-meta">
            <div class="assigned-operator" v-if="transformer.assignedOperator">
              <img 
                :src="transformer.assignedOperator.avatar || '/images/admin-avatar.png'" 
                :alt="transformer.assignedOperator.username"
                class="operator-avatar"
              />
              <span class="operator-name">{{ transformer.assignedOperator.username }}</span>
            </div>
            <div class="last-update">
              {{ formatTime(transformer.updatedAt) }}
            </div>
          </div>
        </div>

        <div class="transformer-actions" v-if="!authStore.isViewer">
          <button 
            class="action-btn status-btn"
            @click.stop="quickStatusUpdate(transformer)"
            :title="'Ubah status'"
          >
            🔄
          </button>
          <button 
            class="action-btn edit-btn"
            @click.stop="editTransformer(transformer)"
            :title="'Edit trafo'"
          >
            ✏️
          </button>
          <button 
            v-if="authStore.isAdmin"
            class="action-btn delete-btn"
            @click.stop="deleteTransformer(transformer)"
            :title="'Hapus trafo'"
          >
            🗑️
          </button>
        </div>
      </div>

      <!-- No Results -->
      <div v-if="filteredTransformers.length === 0" class="no-results">
        <div class="no-results-icon">🔍</div>
        <h3>Tidak ada trafo ditemukan</h3>
        <p>Coba ubah filter atau kata kunci pencarian</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Memuat data trafo...</p>
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

    <!-- Create/Edit Modal -->
    <TransformerModal
      v-if="showCreateModal || showEditModal"
      :transformer="selectedTransformer"
      :is-edit="showEditModal"
      @close="closeModals"
      @save="handleSave"
    />

    <!-- Status Update Modal -->
    <StatusUpdateModal
      v-if="showStatusModal"
      :transformer="selectedTransformer"
      @close="showStatusModal = false"
      @update="handleStatusUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTransformerStore } from '@/stores/transformers'
import { useThemeStore } from '@/stores/theme'
import type { Transformer, TransformerStatus } from '@/types'
import TransformerModal from '@/components/TransformerModal.vue'
import StatusUpdateModal from '@/components/StatusUpdateModal.vue'

const router = useRouter()
const authStore = useAuthStore()
const transformerStore = useTransformerStore()
const themeStore = useThemeStore()

// Reactive data
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const priorityFilter = ref('')
const sortBy = ref('name')
const currentPage = ref(1)
const itemsPerPage = 12

// Modal states
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showStatusModal = ref(false)
const selectedTransformer = ref<Transformer | null>(null)

// Computed properties
const filteredTransformers = computed(() => {
  let filtered = [...transformerStore.transformers]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(t => 
      t.name.toLowerCase().includes(query) ||
      t.transformerCode.toLowerCase().includes(query) ||
      t.locationName.toLowerCase().includes(query)
    )
  }

  // Status filter
  if (statusFilter.value) {
    filtered = filtered.filter(t => t.currentStatus === statusFilter.value)
  }

  // Priority filter
  if (priorityFilter.value) {
    filtered = filtered.filter(t => t.priorityLevel === parseInt(priorityFilter.value))
  }

  // Sorting
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'status':
        return a.currentStatus.localeCompare(b.currentStatus)
      case 'priority':
        return b.priorityLevel - a.priorityLevel
      case 'updatedAt':
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      default:
        return 0
    }
  })

  return filtered
})

const paginatedTransformers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredTransformers.value.slice(start, end)
})

const totalPages = computed(() => 
  Math.ceil(filteredTransformers.value.length / itemsPerPage)
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

const activeCount = computed(() => 
  filteredTransformers.value.filter(t => 
    ['in-progress', 'testing'].includes(t.currentStatus)
  ).length
)

const emergencyCount = computed(() => 
  filteredTransformers.value.filter(t => t.currentStatus === 'emergency').length
)

// Methods
const getStatusText = (status: TransformerStatus): string => {
  const statusTexts: Record<TransformerStatus, string> = {
    'pending': 'Menunggu',
    'in-progress': 'Dikerjakan',
    'testing': 'Testing',
    'completed': 'Selesai',
    'maintenance': 'Maintenance',
    'emergency': 'Emergency',
    'offline': 'Offline'
  }
  return statusTexts[status] || status
}

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / 86400000)
  
  if (days === 0) return 'Hari ini'
  if (days === 1) return 'Kemarin'
  return `${days} hari lalu`
}

const viewTransformer = (id: number) => {
  router.push(`/transformers/${id}`)
}

const editTransformer = (transformer: Transformer) => {
  selectedTransformer.value = transformer
  showEditModal.value = true
}

const quickStatusUpdate = (transformer: Transformer) => {
  selectedTransformer.value = transformer
  showStatusModal.value = true
}

const deleteTransformer = async (transformer: Transformer) => {
  if (!confirm(`Hapus trafo ${transformer.name}?`)) return
  
  try {
    await transformerStore.deleteTransformer(transformer.id)
    // Show success notification
  } catch (error) {
    console.error('Error deleting transformer:', error)
    // Show error notification
  }
}

const refreshData = async () => {
  loading.value = true
  try {
    await transformerStore.fetchTransformers()
  } catch (error) {
    console.error('Error refreshing data:', error)
  } finally {
    loading.value = false
  }
}

const closeModals = () => {
  showCreateModal.value = false
  showEditModal.value = false
  selectedTransformer.value = null
}

const handleSave = async (transformerData: any) => {
  try {
    if (showEditModal.value && selectedTransformer.value) {
      await transformerStore.updateTransformer(selectedTransformer.value.id, transformerData)
    } else {
      await transformerStore.createTransformer(transformerData)
    }
    closeModals()
    // Show success notification
  } catch (error) {
    console.error('Error saving transformer:', error)
    // Show error notification
  }
}

const handleStatusUpdate = async (data: { status: TransformerStatus, reason?: string }) => {
  if (!selectedTransformer.value) return
  
  try {
    await transformerStore.updateTransformerStatus(
      selectedTransformer.value.id,
      data.status,
      data.reason
    )
    showStatusModal.value = false
    selectedTransformer.value = null
    // Show success notification
  } catch (error) {
    console.error('Error updating status:', error)
    // Show error notification
  }
}

// Watch for filter changes to reset pagination
watch([searchQuery, statusFilter, priorityFilter], () => {
  currentPage.value = 1
})

onMounted(async () => {
  await refreshData()
})
</script>

<style scoped>
.transformer-list {
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

.filters-section {
  padding: var(--space-6);
  margin-bottom: var(--space-8);
}

.filters-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
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

.filter-stats {
  display: flex;
  gap: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-color);
}

.stat-item {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.stat-item strong {
  color: var(--text-primary);
}

.transformers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.transformer-card {
  position: relative;
  cursor: pointer;
  transition: all var(--transition-normal);
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  border: 1px solid var(--border-color);
  padding: var(--space-6);
}

.transformer-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-xl);
  border-color: var(--primary-color);
}

.transformer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.transformer-id {
  font-family: var(--font-mono);
  font-weight: 600;
  color: var(--primary-color);
  background: rgba(0, 212, 255, 0.1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
}

.priority-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  box-shadow: 0 0 8px currentColor;
}

.transformer-content {
  margin-bottom: var(--space-4);
}

.transformer-name {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.transformer-location {
  color: var(--text-secondary);
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-sm);
}

.transformer-specs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.spec-label {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.spec-value {
  color: var(--text-primary);
  font-weight: 500;
  font-size: var(--font-size-sm);
}

.transformer-status-section {
  margin-bottom: var(--space-4);
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.progress-text {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.transformer-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assigned-operator {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.operator-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
}

.operator-name {
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
}

.last-update {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.transformer-actions {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  display: flex;
  gap: var(--space-2);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.transformer-card:hover .transformer-actions {
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

.status-btn {
  background: rgba(0, 212, 255, 0.2);
  color: var(--primary-color);
}

.edit-btn {
  background: rgba(255, 215, 0, 0.2);
  color: var(--status-maintenance);
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
  .filters-grid {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-3);
  }
  
  .transformers-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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
  
  .filters-grid {
    grid-template-columns: 1fr;
  }
  
  .transformers-grid {
    grid-template-columns: 1fr;
  }
  
  .transformer-card {
    padding: var(--space-4);
  }
  
  .transformer-actions {
    position: static;
    opacity: 1;
    justify-content: center;
    margin-top: var(--space-4);
  }
}
</style>
