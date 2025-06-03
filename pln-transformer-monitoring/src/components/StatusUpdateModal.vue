<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Update Status Trafo</h2>
        <button class="modal-close" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <div class="transformer-info">
          <div class="transformer-details">
            <h3>{{ transformer?.name }}</h3>
            <p class="transformer-code">{{ transformer?.transformerCode }}</p>
            <p class="transformer-location">📍 {{ transformer?.locationName }}</p>
          </div>
          <div class="current-status">
            <span class="status-label">Status Saat Ini:</span>
            <span 
              class="status-badge current" 
              :class="`status-${transformer?.currentStatus}`"
            >
              {{ getStatusText(transformer?.currentStatus || 'pending') }}
            </span>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="status-form">
          <div class="form-group">
            <label for="newStatus" class="form-label">Status Baru *</label>
            <div class="status-options">
              <label 
                v-for="option in statusOptions"
                :key="option.value"
                class="status-option"
                :class="{ selected: form.newStatus === option.value }"
              >
                <input
                  type="radio"
                  :value="option.value"
                  v-model="form.newStatus"
                  class="status-radio"
                />
                <div class="status-option-content">
                  <div class="status-icon" :style="{ backgroundColor: getStatusColor(option.value) }">
                    {{ option.icon }}
                  </div>
                  <div class="status-text">
                    <div class="status-name">{{ option.label }}</div>
                    <div class="status-desc">{{ option.description }}</div>
                  </div>
                </div>
              </label>
            </div>
          </div>

          <div class="form-group" v-if="form.newStatus">
            <label for="progress" class="form-label">
              Progress ({{ predictedProgress }}%)
            </label>
            <div class="progress-container">
              <input
                id="progress"
                v-model.number="form.progress"
                type="range"
                min="0"
                max="100"
                class="progress-slider"
              />
              <div class="progress-display">
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ 
                      width: form.progress + '%',
                      backgroundColor: getStatusColor(form.newStatus)
                    }"
                  ></div>
                </div>
                <span class="progress-value">{{ form.progress }}%</span>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label for="reason" class="form-label">Alasan Perubahan</label>
            <textarea
              id="reason"
              v-model="form.reason"
              class="form-input"
              rows="3"
              placeholder="Jelaskan alasan perubahan status..."
            ></textarea>
          </div>

          <div class="status-timeline" v-if="transformer">
            <h4>Riwayat Status</h4>
            <div class="timeline-list">
              <div 
                v-for="history in recentHistory" 
                :key="history.id"
                class="timeline-item"
              >
                <div class="timeline-dot" :style="{ backgroundColor: getStatusColor(history.newStatus) }"></div>
                <div class="timeline-content">
                  <div class="timeline-status">
                    {{ getStatusText(history.newStatus) }}
                  </div>
                  <div class="timeline-meta">
                    <span class="timeline-user">{{ history.user?.username }}</span>
                    <span class="timeline-time">{{ formatTime(history.changedAt) }}</span>
                  </div>
                  <div class="timeline-reason" v-if="history.changeReason">
                    "{{ history.changeReason }}"
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn-ghost" @click="closeModal">
              Batal
            </button>
            <button 
              type="submit" 
              class="btn btn-primary" 
              :disabled="!form.newStatus || loading"
            >
              <span v-if="loading" class="spinner"></span>
              <span v-else>Update Status</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useTransformerStore } from '@/stores/transformers'
import { useThemeStore } from '@/stores/theme'
import type { Transformer, TransformerStatus, StatusHistory } from '@/types'

interface Props {
  transformer: Transformer | null
}

interface Emits {
  (e: 'close'): void
  (e: 'update', data: { status: TransformerStatus, reason?: string, progress: number }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const transformerStore = useTransformerStore()
const themeStore = useThemeStore()
const loading = ref(false)

const form = reactive({
  newStatus: '' as TransformerStatus | '',
  reason: '',
  progress: 0
})

const statusOptions = [
  {
    value: 'pending' as TransformerStatus,
    label: 'Menunggu',
    icon: '⏳',
    description: 'Menunggu untuk dikerjakan'
  },
  {
    value: 'in-progress' as TransformerStatus,
    label: 'Dikerjakan',
    icon: '🔧',
    description: 'Sedang dalam proses perbaikan'
  },
  {
    value: 'testing' as TransformerStatus,
    label: 'Testing',
    icon: '🧪',
    description: 'Dalam tahap pengujian'
  },
  {
    value: 'completed' as TransformerStatus,
    label: 'Selesai',
    icon: '✅',
    description: 'Pekerjaan telah selesai'
  },
  {
    value: 'maintenance' as TransformerStatus,
    label: 'Maintenance',
    icon: '🛠️',
    description: 'Dalam jadwal maintenance'
  },
  {
    value: 'emergency' as TransformerStatus,
    label: 'Emergency',
    icon: '🚨',
    description: 'Memerlukan perhatian segera'
  },
  {
    value: 'offline' as TransformerStatus,
    label: 'Offline',
    icon: '⚫',
    description: 'Tidak beroperasi'
  }
]

const predictedProgress = computed(() => {
  if (!form.newStatus) return 0
  
  const progressMap: Record<TransformerStatus, number> = {
    'pending': 0,
    'in-progress': 25,
    'testing': 75,
    'completed': 100,
    'maintenance': 50,
    'emergency': 10,
    'offline': 0
  }
  
  return progressMap[form.newStatus as TransformerStatus] || 0
})

const recentHistory = computed((): StatusHistory[] => {
  if (!props.transformer) return []
  
  return transformerStore.getStatusHistoryForTransformer(props.transformer.id)
    .slice(0, 5)
    .sort((a, b) => new Date(b.changedAt).getTime() - new Date(a.changedAt).getTime())
})

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

const getStatusColor = (status: TransformerStatus): string => {
  return themeStore.getStatusColor(status)
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

const closeModal = () => {
  emit('close')
}

const handleSubmit = async () => {
  if (!form.newStatus) return
  
  loading.value = true
  
  try {
    emit('update', {
      status: form.newStatus as TransformerStatus,
      reason: form.reason || undefined,
      progress: form.progress
    })
  } catch (error) {
    console.error('Error updating status:', error)
  } finally {
    loading.value = false
  }
}

// Watch for status changes to auto-update progress
import { watch } from 'vue'
watch(() => form.newStatus, (newStatus) => {
  if (newStatus) {
    form.progress = predictedProgress.value
  }
})

onMounted(() => {
  if (props.transformer) {
    form.progress = props.transformer.progress
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
}

.modal-content {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, var(--bg-tertiary), var(--bg-secondary));
}

.modal-header h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: var(--font-size-xl);
}

.modal-close {
  width: 32px;
  height: 32px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all var(--transition-normal);
}

.modal-close:hover {
  background: var(--status-emergency);
  color: white;
  border-color: var(--status-emergency);
}

.modal-body {
  padding: var(--space-6);
}

.transformer-info {
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  margin-bottom: var(--space-6);
  border: 1px solid var(--border-color);
}

.transformer-details h3 {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
}

.transformer-code {
  font-family: var(--font-mono);
  color: var(--primary-color);
  font-weight: 600;
  margin: 0 0 var(--space-1) 0;
}

.transformer-location {
  color: var(--text-secondary);
  margin: 0 0 var(--space-3) 0;
  font-size: var(--font-size-sm);
}

.current-status {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.status-label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.status-badge.current {
  font-weight: 600;
}

.status-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.form-label {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.status-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}

.status-option {
  cursor: pointer;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  background: var(--bg-primary);
  transition: all var(--transition-normal);
  position: relative;
}

.status-option:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.status-option.selected {
  border-color: var(--primary-color);
  background: rgba(0, 212, 255, 0.1);
  box-shadow: var(--glow-primary);
}

.status-radio {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.status-option-content {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
  font-weight: bold;
}

.status-text {
  flex: 1;
  min-width: 0;
}

.status-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.status-desc {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  line-height: 1.3;
}

.progress-container {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.progress-slider {
  width: 100%;
  height: 6px;
  border-radius: var(--radius-sm);
  background: var(--bg-tertiary);
  outline: none;
  cursor: pointer;
}

.progress-slider::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--primary-color);
  cursor: pointer;
  box-shadow: var(--glow-primary);
}

.progress-slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--primary-color);
  cursor: pointer;
  border: none;
  box-shadow: var(--glow-primary);
}

.progress-display {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

.progress-value {
  font-weight: 600;
  color: var(--text-primary);
  min-width: 45px;
  text-align: right;
}

.form-input {
  padding: var(--space-3);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  resize: vertical;
  min-height: 80px;
  transition: all var(--transition-normal);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.status-timeline {
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  border: 1px solid var(--border-color);
}

.status-timeline h4 {
  margin: 0 0 var(--space-4) 0;
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  box-shadow: 0 0 8px currentColor;
}

.timeline-content {
  flex: 1;
  min-width: 0;
}

.timeline-status {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.timeline-meta {
  display: flex;
  gap: var(--space-2);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-bottom: var(--space-1);
}

.timeline-user {
  font-weight: 500;
}

.timeline-reason {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  font-style: italic;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-color);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Responsive Design */
@media (max-width: 768px) {
  .modal-overlay {
    padding: var(--space-2);
  }
  
  .modal-content {
    max-height: 95vh;
  }
  
  .modal-header,
  .modal-body {
    padding: var(--space-4);
  }
  
  .status-options {
    grid-template-columns: 1fr;
  }
  
  .modal-actions {
    flex-direction: column-reverse;
  }
  
  .modal-actions button {
    width: 100%;
  }
}
</style>
