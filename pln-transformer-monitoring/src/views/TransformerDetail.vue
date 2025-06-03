<template>
  <div class="transformer-detail" v-if="transformer">
    <!-- Header -->
    <div class="detail-header">
      <div class="header-left">
        <button class="btn btn-ghost" @click="goBack">
          ← Kembali
        </button>
        <div class="header-info">
          <h1>{{ transformer.name }}</h1>
          <div class="header-meta">
            <span class="transformer-code">{{ transformer.transformerCode }}</span>
            <span 
              class="status-badge large" 
              :class="`status-${transformer.currentStatus}`"
            >
              {{ getStatusText(transformer.currentStatus) }}
            </span>
          </div>
        </div>
      </div>
      <div class="header-actions" v-if="!authStore.isViewer">
        <button class="btn btn-secondary" @click="showStatusModal = true">
          🔄 Update Status
        </button>
        <button class="btn btn-primary" @click="showEditModal = true">
          ✏️ Edit
        </button>
      </div>
    </div>

    <!-- Content Grid -->
    <div class="detail-content">
      <!-- Left Column -->
      <div class="detail-left">
        <!-- Basic Info -->
        <div class="info-card card">
          <div class="card-header">
            <h3>Informasi Dasar</h3>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Kode Trafo</span>
              <span class="info-value">{{ transformer.transformerCode }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Nama</span>
              <span class="info-value">{{ transformer.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Tipe</span>
              <span class="info-value">{{ transformer.type }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Kapasitas</span>
              <span class="info-value">{{ transformer.capacityKva }} kVA</span>
            </div>
            <div class="info-item">
              <span class="info-label">Tegangan</span>
              <span class="info-value">{{ transformer.primaryVoltage }}/{{ transformer.secondaryVoltage }} kV</span>
            </div>
            <div class="info-item">
              <span class="info-label">Prioritas</span>
              <span class="priority-badge" :style="{ backgroundColor: themeStore.getPriorityColor(transformer.priorityLevel) }">
                Level {{ transformer.priorityLevel }}
              </span>
            </div>
          </div>
        </div>

        <!-- Location Info -->
        <div class="location-card card">
          <div class="card-header">
            <h3>Lokasi</h3>
          </div>
          <div class="location-content">
            <div class="location-address">
              <span class="location-icon">📍</span>
              <span class="location-text">{{ transformer.locationName }}</span>
            </div>
            <div class="coordinates" v-if="transformer.latitude && transformer.longitude">
              <span class="coord-label">Koordinat:</span>
              <span class="coord-value">
                {{ transformer.latitude.toFixed(6) }}, {{ transformer.longitude.toFixed(6) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Progress -->
        <div class="progress-card card">
          <div class="card-header">
            <h3>Progress Pekerjaan</h3>
          </div>
          <div class="progress-content">
            <div class="progress-info">
              <span class="progress-text">{{ transformer.progress }}% Selesai</span>
              <span class="progress-eta">{{ getProgressStatus(transformer.progress) }}</span>
            </div>
            <div class="progress-bar large">
              <div 
                class="progress-fill" 
                :style="{ 
                  width: transformer.progress + '%',
                  backgroundColor: themeStore.getStatusColor(transformer.currentStatus)
                }"
              ></div>
            </div>
          </div>
        </div>

        <!-- Notes -->
        <div class="notes-card card" v-if="transformer.notes">
          <div class="card-header">
            <h3>Catatan</h3>
          </div>
          <div class="notes-content">
            <p>{{ transformer.notes }}</p>
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div class="detail-right">
        <!-- Schedule Info -->
        <div class="schedule-card card">
          <div class="card-header">
            <h3>Jadwal</h3>
          </div>
          <div class="schedule-content">
            <div class="schedule-item" v-if="transformer.installationDate">
              <span class="schedule-label">Instalasi</span>
              <span class="schedule-value">{{ formatDate(transformer.installationDate) }}</span>
            </div>
            <div class="schedule-item" v-if="transformer.lastMaintenanceDate">
              <span class="schedule-label">Maintenance Terakhir</span>
              <span class="schedule-value">{{ formatDate(transformer.lastMaintenanceDate) }}</span>
            </div>
            <div class="schedule-item" v-if="transformer.nextMaintenanceDate">
              <span class="schedule-label">Maintenance Berikutnya</span>
              <span class="schedule-value" :class="{ overdue: isOverdue(transformer.nextMaintenanceDate) }">
                {{ formatDate(transformer.nextMaintenanceDate) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Assigned Operator -->
        <div class="operator-card card" v-if="transformer.assignedOperator">
          <div class="card-header">
            <h3>PIC/Operator</h3>
          </div>
          <div class="operator-content">
            <div class="operator-info">
              <img 
                :src="transformer.assignedOperator.avatar || '/images/admin-avatar.png'"
                :alt="transformer.assignedOperator.username"
                class="operator-avatar"
              />
              <div class="operator-details">
                <div class="operator-name">{{ transformer.assignedOperator.username }}</div>
                <div class="operator-role">{{ transformer.assignedOperator.role }}</div>
                <div class="operator-email">{{ transformer.assignedOperator.email }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Status History -->
        <div class="history-card card">
          <div class="card-header">
            <h3>Riwayat Status</h3>
          </div>
          <div class="history-content">
            <div class="history-list">
              <div 
                v-for="history in statusHistory" 
                :key="history.id"
                class="history-item"
              >
                <div class="history-dot" :style="{ backgroundColor: themeStore.getStatusColor(history.newStatus) }"></div>
                <div class="history-info">
                  <div class="history-status">
                    {{ getStatusText(history.newStatus) }}
                  </div>
                  <div class="history-meta">
                    <span class="history-user">{{ history.user?.username }}</span>
                    <span class="history-time">{{ formatTime(history.changedAt) }}</span>
                  </div>
                  <div class="history-reason" v-if="history.changeReason">
                    "{{ history.changeReason }}"
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <TransformerModal
      v-if="showEditModal"
      :transformer="transformer"
      :is-edit="true"
      @close="showEditModal = false"
      @save="handleSave"
    />

    <StatusUpdateModal
      v-if="showStatusModal"
      :transformer="transformer"
      @close="showStatusModal = false"
      @update="handleStatusUpdate"
    />
  </div>

  <!-- Loading State -->
  <div v-else-if="loading" class="loading-state">
    <div class="spinner"></div>
    <p>Memuat detail trafo...</p>
  </div>

  <!-- Not Found -->
  <div v-else class="not-found">
    <div class="not-found-icon">🔍</div>
    <h2>Trafo Tidak Ditemukan</h2>
    <p>Trafo dengan ID {{ $route.params.id }} tidak ditemukan.</p>
    <button class="btn btn-primary" @click="goBack">
      Kembali
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTransformerStore } from '@/stores/transformers'
import { useThemeStore } from '@/stores/theme'
import type { Transformer, TransformerStatus, StatusHistory } from '@/types'
import TransformerModal from '@/components/TransformerModal.vue'
import StatusUpdateModal from '@/components/StatusUpdateModal.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const transformerStore = useTransformerStore()
const themeStore = useThemeStore()

const loading = ref(false)
const showEditModal = ref(false)
const showStatusModal = ref(false)

const transformerId = computed(() => parseInt(route.params.id as string))

const transformer = computed(() => 
  transformerStore.getTransformerById(transformerId.value)
)

const statusHistory = computed(() => 
  transformerStore.getStatusHistoryForTransformer(transformerId.value)
    .slice(0, 10)
    .sort((a, b) => new Date(b.changedAt).getTime() - new Date(a.changedAt).getTime())
)

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

const getProgressStatus = (progress: number): string => {
  if (progress === 0) return 'Belum dimulai'
  if (progress < 25) return 'Baru dimulai'
  if (progress < 50) return 'Dalam progress'
  if (progress < 75) return 'Hampir selesai'
  if (progress < 100) return 'Tahap akhir'
  return 'Selesai'
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('id-ID', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / 3600000)
  
  if (hours < 1) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes} menit lalu`
  }
  if (hours < 24) return `${hours} jam lalu`
  
  const days = Math.floor(hours / 24)
  return `${days} hari lalu`
}

const isOverdue = (dateString: string): boolean => {
  return new Date(dateString) < new Date()
}

const goBack = () => {
  router.push('/transformers')
}

const handleSave = async (transformerData: any) => {
  try {
    await transformerStore.updateTransformer(transformerId.value, transformerData)
    showEditModal.value = false
    // Show success notification
  } catch (error) {
    console.error('Error updating transformer:', error)
    // Show error notification
  }
}

const handleStatusUpdate = async (data: { status: TransformerStatus, reason?: string }) => {
  try {
    await transformerStore.updateTransformerStatus(
      transformerId.value,
      data.status,
      data.reason
    )
    showStatusModal.value = false
    // Show success notification
  } catch (error) {
    console.error('Error updating status:', error)
    // Show error notification
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      transformerStore.fetchTransformers(),
      transformerStore.fetchStatusHistory()
    ])
  } catch (error) {
    console.error('Error loading transformer data:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.transformer-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-6);
  border-bottom: 1px solid var(--border-color);
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
}

.header-info h1 {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
  font-size: var(--font-size-3xl);
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.transformer-code {
  font-family: var(--font-mono);
  background: rgba(0, 212, 255, 0.1);
  color: var(--primary-color);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: var(--font-size-sm);
}

.status-badge.large {
  font-size: var(--font-size-base);
  padding: var(--space-2) var(--space-4);
}

.header-actions {
  display: flex;
  gap: var(--space-3);
}

.detail-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-8);
}

.detail-left,
.detail-right {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.card-header {
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-color);
}

.card-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.info-label {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 500;
}

.info-value {
  color: var(--text-primary);
  font-weight: 600;
}

.priority-badge {
  color: white;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  align-self: flex-start;
}

.location-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.location-address {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
}

.location-icon {
  font-size: 18px;
  margin-top: 2px;
}

.location-text {
  color: var(--text-primary);
  line-height: 1.5;
}

.coordinates {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.coord-label {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.coord-value {
  font-family: var(--font-mono);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.progress-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-text {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
}

.progress-eta {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.progress-bar.large {
  height: 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.notes-content p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.schedule-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.schedule-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-3);
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.schedule-label {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.schedule-value {
  color: var(--text-primary);
  font-weight: 600;
}

.schedule-value.overdue {
  color: var(--status-emergency);
}

.operator-content {
  display: flex;
  flex-direction: column;
}

.operator-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.operator-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 3px solid var(--primary-color);
  object-fit: cover;
}

.operator-details {
  flex: 1;
  min-width: 0;
}

.operator-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.operator-role {
  color: var(--primary-color);
  font-size: var(--font-size-sm);
  text-transform: uppercase;
  font-weight: 500;
  margin-bottom: var(--space-1);
}

.operator-email {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.history-content {
  max-height: 400px;
  overflow-y: auto;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.history-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
}

.history-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  box-shadow: 0 0 8px currentColor;
}

.history-info {
  flex: 1;
  min-width: 0;
}

.history-status {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.history-meta {
  display: flex;
  gap: var(--space-2);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-bottom: var(--space-1);
}

.history-user {
  font-weight: 500;
}

.history-reason {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  font-style: italic;
}

.loading-state,
.not-found {
  text-align: center;
  padding: var(--space-16);
}

.not-found-icon {
  font-size: 64px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.not-found h2 {
  margin-bottom: var(--space-2);
  color: var(--text-secondary);
}

.not-found p {
  color: var(--text-muted);
  margin-bottom: var(--space-6);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .detail-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    gap: var(--space-4);
    align-items: stretch;
  }
  
  .header-left {
    flex-direction: column;
    gap: var(--space-3);
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .header-info h1 {
    font-size: var(--font-size-2xl);
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .progress-info {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }
}
</style>
