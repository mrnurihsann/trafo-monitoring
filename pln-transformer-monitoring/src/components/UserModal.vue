<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>{{ isEdit ? 'Edit Pengguna' : 'Tambah Pengguna Baru' }}</h2>
        <button class="modal-close" @click="closeModal">✕</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-form">
        <div class="form-grid">
          <!-- Basic Info -->
          <div class="form-section">
            <h3>Informasi Dasar</h3>
            
            <div class="form-group">
              <label for="username" class="form-label">Username *</label>
              <input
                id="username"
                v-model="form.username"
                type="text"
                class="form-input"
                placeholder="john_doe"
                required
                :readonly="isEdit"
              />
              <span class="form-hint" v-if="isEdit">Username tidak dapat diubah</span>
            </div>

            <div class="form-group">
              <label for="email" class="form-label">Email *</label>
              <input
                id="email"
                v-model="form.email"
                type="email"
                class="form-input"
                placeholder="user@example.com"
                required
              />
            </div>

            <div class="form-group">
              <label for="role" class="form-label">Role *</label>
              <select id="role" v-model="form.role" class="form-select" required>
                <option value="">Pilih Role</option>
                <option value="admin">Admin</option>
                <option value="operator">Operator</option>
                <option value="viewer">Viewer</option>
              </select>
            </div>
          </div>

          <!-- Password Section -->
          <div class="form-section" v-if="!isEdit">
            <h3>Password</h3>
            
            <div class="form-group">
              <label for="password" class="form-label">Password *</label>
              <div class="password-input">
                <input
                  id="password"
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input"
                  placeholder="Minimal 8 karakter"
                  required
                  minlength="8"
                />
                <button 
                  type="button"
                  @click="showPassword = !showPassword"
                  class="password-toggle"
                >
                  {{ showPassword ? '👁️' : '🙈' }}
                </button>
              </div>
              <div class="password-strength">
                <div class="strength-bar">
                  <div 
                    class="strength-fill" 
                    :class="passwordStrength.level"
                    :style="{ width: passwordStrength.percentage + '%' }"
                  ></div>
                </div>
                <span class="strength-text">{{ passwordStrength.text }}</span>
              </div>
            </div>

            <div class="form-group">
              <label for="confirmPassword" class="form-label">Konfirmasi Password *</label>
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="Ulangi password"
                required
              />
              <div class="password-match" v-if="form.confirmPassword">
                <span v-if="passwordsMatch" class="match-success">✅ Password cocok</span>
                <span v-else class="match-error">❌ Password tidak cocok</span>
              </div>
            </div>
          </div>

          <!-- Role Information -->
          <div class="form-section full-width">
            <h3>Informasi Role</h3>
            
            <div class="role-info" v-if="form.role">
              <div class="role-card" :class="form.role">
                <div class="role-icon">
                  {{ getRoleIcon(form.role) }}
                </div>
                <div class="role-details">
                  <div class="role-name">{{ getRoleName(form.role) }}</div>
                  <div class="role-description">{{ getRoleDescription(form.role) }}</div>
                  <div class="role-permissions">
                    <h4>Permissions:</h4>
                    <ul>
                      <li v-for="permission in getRolePermissions(form.role)" :key="permission">
                        {{ permission }}
                      </li>
                    </ul>
                  </div>
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
            :disabled="!isFormValid || loading"
          >
            <span v-if="loading" class="spinner"></span>
            <span v-else>{{ isEdit ? 'Update' : 'Simpan' }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { User } from '@/types'

interface Props {
  user?: User | null
  isEdit?: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'save', data: any): void
}

const props = withDefaults(defineProps<Props>(), {
  user: null,
  isEdit: false
})

const emit = defineEmits<Emits>()

const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const form = reactive({
  username: '',
  email: '',
  role: '',
  password: '',
  confirmPassword: ''
})

const passwordStrength = computed(() => {
  const password = form.password
  if (!password) return { level: '', percentage: 0, text: '' }

  let score = 0
  let feedback = []

  // Length check
  if (password.length >= 8) score += 20
  else feedback.push('minimal 8 karakter')

  // Lowercase check
  if (/[a-z]/.test(password)) score += 20
  else feedback.push('huruf kecil')

  // Uppercase check
  if (/[A-Z]/.test(password)) score += 20
  else feedback.push('huruf besar')

  // Number check
  if (/\d/.test(password)) score += 20
  else feedback.push('angka')

  // Special character check
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score += 20
  else feedback.push('karakter khusus')

  let level = ''
  let text = ''

  if (score < 40) {
    level = 'weak'
    text = `Lemah - tambahkan: ${feedback.join(', ')}`
  } else if (score < 60) {
    level = 'fair'
    text = `Cukup - tambahkan: ${feedback.join(', ')}`
  } else if (score < 80) {
    level = 'good'
    text = 'Bagus'
  } else {
    level = 'strong'
    text = 'Kuat'
  }

  return { level, percentage: score, text }
})

const passwordsMatch = computed(() => {
  return form.password === form.confirmPassword
})

const isFormValid = computed(() => {
  const basicValid = form.username && form.email && form.role
  
  if (props.isEdit) {
    return basicValid
  } else {
    return basicValid && 
           form.password && 
           form.password.length >= 8 && 
           passwordsMatch.value
  }
})

const closeModal = () => {
  emit('close')
}

const handleSubmit = async () => {
  if (!isFormValid.value) return
  
  loading.value = true
  
  try {
    const submitData = {
      username: form.username,
      email: form.email,
      role: form.role as 'admin' | 'operator' | 'viewer'
    }

    // Only include password for new users
    if (!props.isEdit) {
      Object.assign(submitData, { password: form.password })
    }

    emit('save', submitData)
  } catch (error) {
    console.error('Error submitting form:', error)
  } finally {
    loading.value = false
  }
}

const getRoleIcon = (role: string): string => {
  const icons = {
    admin: '👑',
    operator: '👷',
    viewer: '👁️'
  }
  return icons[role as keyof typeof icons] || '👤'
}

const getRoleName = (role: string): string => {
  const names = {
    admin: 'Administrator',
    operator: 'Operator',
    viewer: 'Viewer'
  }
  return names[role as keyof typeof names] || role
}

const getRoleDescription = (role: string): string => {
  const descriptions = {
    admin: 'Akses penuh ke semua fitur sistem',
    operator: 'Dapat mengelola dan mengupdate data trafo',
    viewer: 'Hanya dapat melihat data tanpa mengubah'
  }
  return descriptions[role as keyof typeof descriptions] || ''
}

const getRolePermissions = (role: string): string[] => {
  const permissions = {
    admin: [
      'Kelola semua data trafo',
      'Manajemen pengguna',
      'Akses laporan lengkap',
      'Konfigurasi sistem',
      'Export data'
    ],
    operator: [
      'Update status trafo',
      'Edit data trafo yang ditugaskan',
      'Buat work order',
      'Lihat laporan terbatas'
    ],
    viewer: [
      'Lihat dashboard',
      'Lihat data trafo',
      'Lihat laporan read-only'
    ]
  }
  return permissions[role as keyof typeof permissions] || []
}

const populateForm = () => {
  if (props.user && props.isEdit) {
    form.username = props.user.username
    form.email = props.user.email
    form.role = props.user.role
  }
}

// Initialize form when component mounts
populateForm()
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
  max-width: 800px;
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
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
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

.form-input:read-only {
  background: var(--bg-tertiary);
  color: var(--text-muted);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.form-hint {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-style: italic;
}

.password-input {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-muted);
  transition: color var(--transition-normal);
}

.password-toggle:hover {
  color: var(--text-primary);
}

.password-strength {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-2);
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

.strength-fill.weak { background: #ff4444; }
.strength-fill.fair { background: #ffa500; }
.strength-fill.good { background: #00d4ff; }
.strength-fill.strong { background: #00ff88; }

.strength-text {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  min-width: 120px;
}

.password-match {
  margin-top: var(--space-2);
  font-size: var(--font-size-xs);
}

.match-success {
  color: var(--accent-color);
}

.match-error {
  color: var(--status-emergency);
}

.role-info {
  margin-top: var(--space-4);
}

.role-card {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  border: 2px solid var(--border-color);
  background: var(--bg-tertiary);
}

.role-card.admin {
  border-color: var(--status-emergency);
  background: rgba(255, 68, 68, 0.05);
}

.role-card.operator {
  border-color: var(--primary-color);
  background: rgba(0, 212, 255, 0.05);
}

.role-card.viewer {
  border-color: var(--accent-color);
  background: rgba(0, 255, 136, 0.05);
}

.role-icon {
  font-size: 40px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  min-width: 60px;
}

.role-details {
  flex: 1;
}

.role-name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.role-description {
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.role-permissions h4 {
  color: var(--text-primary);
  font-size: var(--font-size-sm);
  margin: 0 0 var(--space-2) 0;
}

.role-permissions ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.role-permissions li {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-1);
  position: relative;
  padding-left: var(--space-4);
}

.role-permissions li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--accent-color);
  font-weight: bold;
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
  
  .role-card {
    flex-direction: column;
    text-align: center;
  }
  
  .role-icon {
    min-width: auto;
  }
  
  .modal-actions {
    flex-direction: column-reverse;
  }
  
  .modal-actions button {
    width: 100%;
  }
}
</style>
