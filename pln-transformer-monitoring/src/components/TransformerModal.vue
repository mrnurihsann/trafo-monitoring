<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>{{ isEdit ? 'Edit Trafo' : 'Tambah Trafo Baru' }}</h2>
        <button class="modal-close" @click="closeModal">✕</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-form">
        <div class="form-grid">
          <!-- Basic Info -->
          <div class="form-section">
            <h3>Informasi Dasar</h3>
            
            <div class="form-group">
              <label for="transformerCode" class="form-label">Kode Trafo *</label>
              <input
                id="transformerCode"
                v-model="form.transformerCode"
                type="text"
                class="form-input"
                placeholder="TRF001"
                required
              />
            </div>

            <div class="form-group">
              <label for="name" class="form-label">Nama Trafo *</label>
              <input
                id="name"
                v-model="form.name"
                type="text"
                class="form-input"
                placeholder="Trafo Utama Area A"
                required
              />
            </div>

            <div class="form-group">
              <label for="type" class="form-label">Tipe Trafo *</label>
              <select id="type" v-model="form.type" class="form-select" required>
                <option value="">Pilih Tipe</option>
                <option value="Distribution">Distribution</option>
                <option value="Power">Power</option>
                <option value="Instrument">Instrument</option>
                <option value="Auto">Auto Transformer</option>
              </select>
            </div>
          </div>

          <!-- Location -->
          <div class="form-section">
            <h3>Lokasi</h3>
            
            <div class="form-group">
              <label for="locationName" class="form-label">Alamat Lokasi *</label>
              <textarea
                id="locationName"
                v-model="form.locationName"
                class="form-input"
                rows="3"
                placeholder="Jl. Sudirman No. 123, Jakarta"
                required
              ></textarea>
            </div>

            <div class="form-group-row">
              <div class="form-group">
                <label for="latitude" class="form-label">Latitude</label>
                <input
                  id="latitude"
                  v-model.number="form.latitude"
                  type="number"
                  step="any"
                  class="form-input"
                  placeholder="-6.2088"
                />
              </div>
              <div class="form-group">
                <label for="longitude" class="form-label">Longitude</label>
                <input
                  id="longitude"
                  v-model.number="form.longitude"
                  type="number"
                  step="any"
                  class="form-input"
                  placeholder="106.8456"
                />
              </div>
            </div>
          </div>

          <!-- Technical Specs -->
          <div class="form-section">
            <h3>Spesifikasi Teknis</h3>
            
            <div class="form-group">
              <label for="capacityKva" class="form-label">Kapasitas (kVA) *</label>
              <input
                id="capacityKva"
                v-model.number="form.capacityKva"
                type="number"
                class="form-input"
                placeholder="1000"
                required
                min="1"
              />
            </div>

            <div class="form-group-row">
              <div class="form-group">
                <label for="primaryVoltage" class="form-label">Tegangan Primer (kV) *</label>
                <input
                  id="primaryVoltage"
                  v-model.number="form.primaryVoltage"
                  type="number"
                  class="form-input"
                  placeholder="20"
                  required
                  min="1"
                />
              </div>
              <div class="form-group">
                <label for="secondaryVoltage" class="form-label">Tegangan Sekunder (kV) *</label>
                <input
                  id="secondaryVoltage"
                  v-model.number="form.secondaryVoltage"
                  type="number"
                  class="form-input"
                  placeholder="0.4"
                  required
                  min="0.1"
                  step="0.1"
                />
              </div>
            </div>
          </div>

          <!-- Dates & Status -->
          <div class="form-section">
            <h3>Status & Jadwal</h3>
            
            <div class="form-group-row">
              <div class="form-group">
                <label for="installationDate" class="form-label">Tanggal Instalasi</label>
                <input
                  id="installationDate"
                  v-model="form.installationDate"
                  type="date"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label for="lastMaintenanceDate" class="form-label">Maintenance Terakhir</label>
                <input
                  id="lastMaintenanceDate"
                  v-model="form.lastMaintenanceDate"
                  type="date"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="nextMaintenanceDate" class="form-label">Maintenance Berikutnya</label>
              <input
                id="nextMaintenanceDate"
                v-model="form.nextMaintenanceDate"
                type="date"
                class="form-input"
              />
            </div>

            <div class="form-group-row">
              <div class="form-group">
                <label for="currentStatus" class="form-label">Status *</label>
                <select id="currentStatus" v-model="form.currentStatus" class="form-select" required>
                  <option value="pending">Menunggu</option>
                  <option value="in-progress">Dikerjakan</option>
                  <option value="testing">Testing</option>
                  <option value="completed">Selesai</option>
                  <option value="maintenance">Maintenance</option>
                  <option value="emergency">Emergency</option>
                  <option value="offline">Offline</option>
                </select>
              </div>
              <div class="form-group">
                <label for="priorityLevel" class="form-label">Prioritas *</label>
                <select id="priorityLevel" v-model.number="form.priorityLevel" class="form-select" required>
                  <option :value="1">1 - Rendah</option>
                  <option :value="2">2 - Normal</option>
                  <option :value="3">3 - Tinggi</option>
                  <option :value="4">4 - Kritis</option>
                  <option :value="5">5 - Emergency</option>
                </select>
              </div>
            </div>

            <div class="form-group" v-if="!authStore.isViewer">
              <label for="assignedOperatorId" class="form-label">PIC/Operator</label>
              <select id="assignedOperatorId" v-model.number="form.assignedOperatorId" class="form-select">
                <option :value="null">Tidak ada</option>
                <option 
                  v-for="user in operators" 
                  :key="user.id" 
                  :value="user.id"
                >
                  {{ user.username }} ({{ user.role }})
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="progress" class="form-label">Progress (%)</label>
              <div class="progress-input-container">
                <input
                  id="progress"
                  v-model.number="form.progress"
                  type="range"
                  min="0"
                  max="100"
                  class="progress-slider"
                />
                <span class="progress-value">{{ form.progress }}%</span>
              </div>
            </div>
          </div>

          <!-- Notes -->
          <div class="form-section full-width">
            <h3>Catatan</h3>
            <div class="form-group">
              <label for="notes" class="form-label">Keterangan</label>
              <textarea
                id="notes"
                v-model="form.notes"
                class="form-input"
                rows="4"
                placeholder="Catatan atau keterangan tambahan..."
              ></textarea>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button type="button" class="btn btn-ghost" @click="closeModal">
            Batal
          </button>
          <button type="submit" class="btn btn-primary" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            <span v-else>{{ isEdit ? 'Update' : 'Simpan' }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { api } from '@/services/api'
import type { Transformer, User, TransformerStatus } from '@/types'

interface Props {
  transformer?: Transformer | null
  isEdit?: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'save', data: any): void
}

const props = withDefaults(defineProps<Props>(), {
  transformer: null,
  isEdit: false
})

const emit = defineEmits<Emits>()

const authStore = useAuthStore()
const loading = ref(false)
const operators = ref<User[]>([])

const form = reactive({
  transformerCode: '',
  name: '',
  type: '',
  locationName: '',
  latitude: null as number | null,
  longitude: null as number | null,
  capacityKva: null as number | null,
  primaryVoltage: null as number | null,
  secondaryVoltage: null as number | null,
  installationDate: '',
  lastMaintenanceDate: '',
  nextMaintenanceDate: '',
  currentStatus: 'pending' as TransformerStatus,
  priorityLevel: 2,
  assignedOperatorId: null as number | null,
  progress: 0,
  notes: ''
})

const closeModal = () => {
  emit('close')
}

const handleSubmit = async () => {
  loading.value = true
  
  try {
    // Prepare data for submission
    const submitData = {
      ...form,
      // Convert empty strings to null for optional fields
      latitude: form.latitude || undefined,
      longitude: form.longitude || undefined,
      installationDate: form.installationDate || undefined,
      lastMaintenanceDate: form.lastMaintenanceDate || undefined,
      nextMaintenanceDate: form.nextMaintenanceDate || undefined,
      assignedOperatorId: form.assignedOperatorId || undefined,
      notes: form.notes || undefined
    }

    emit('save', submitData)
  } catch (error) {
    console.error('Error submitting form:', error)
  } finally {
    loading.value = false
  }
}

const loadOperators = async () => {
  try {
    const users = await api.getUsers()
    operators.value = users.filter(u => ['admin', 'operator'].includes(u.role))
  } catch (error) {
    console.error('Error loading operators:', error)
  }
}

const populateForm = () => {
  if (props.transformer) {
    Object.assign(form, {
      transformerCode: props.transformer.transformerCode,
      name: props.transformer.name,
      type: props.transformer.type,
      locationName: props.transformer.locationName,
      latitude: props.transformer.latitude,
      longitude: props.transformer.longitude,
      capacityKva: props.transformer.capacityKva,
      primaryVoltage: props.transformer.primaryVoltage,
      secondaryVoltage: props.transformer.secondaryVoltage,
      installationDate: props.transformer.installationDate || '',
      lastMaintenanceDate: props.transformer.lastMaintenanceDate || '',
      nextMaintenanceDate: props.transformer.nextMaintenanceDate || '',
      currentStatus: props.transformer.currentStatus,
      priorityLevel: props.transformer.priorityLevel,
      assignedOperatorId: props.transformer.assignedOperatorId,
      progress: props.transformer.progress,
      notes: props.transformer.notes || ''
    })
  }
}

onMounted(() => {
  loadOperators()
  populateForm()
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
  max-width: 900px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
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
  font-size: var(--font-size-2xl);
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

.modal-form {
  padding: var(--space-6);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-8);
  margin-bottom: var(--space-8);
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.form-section.full-width {
  grid-column: 1 / -1;
}

.form-section h3 {
  margin: 0 0 var(--space-4) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
  padding-bottom: var(--space-2);
  border-bottom: 2px solid var(--primary-color);
  position: relative;
}

.form-section h3::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 30px;
  height: 2px;
  background: var(--accent-color);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-group-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.form-label {
  font-weight: 500;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.form-input,
.form-select {
  padding: var(--space-3);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--font-size-base);
  transition: all var(--transition-normal);
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
}

textarea.form-input {
  resize: vertical;
  min-height: 80px;
}

.progress-input-container {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.progress-slider {
  flex: 1;
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

.progress-value {
  font-weight: 600;
  color: var(--primary-color);
  min-width: 50px;
  text-align: right;
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
  
  .modal-header {
    padding: var(--space-4);
  }
  
  .modal-form {
    padding: var(--space-4);
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: var(--space-6);
  }
  
  .form-group-row {
    grid-template-columns: 1fr;
    gap: var(--space-3);
  }
  
  .modal-actions {
    flex-direction: column-reverse;
  }
  
  .modal-actions button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .modal-header h2 {
    font-size: var(--font-size-xl);
  }
  
  .form-section h3 {
    font-size: var(--font-size-base);
  }
}
</style>
