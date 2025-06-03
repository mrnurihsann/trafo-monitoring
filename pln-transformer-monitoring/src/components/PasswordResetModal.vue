<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Reset Password</h2>
        <button class="modal-close" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <div class="user-info" v-if="user">
          <div class="user-avatar">
            <img 
              :src="user.avatar || '/images/admin-avatar.png'" 
              :alt="user.username"
            />
          </div>
          <div class="user-details">
            <h3>{{ user.username }}</h3>
            <p class="user-email">{{ user.email }}</p>
            <span class="user-role-badge" :class="user.role">
              {{ user.role }}
            </span>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="reset-form">
          <div class="form-group">
            <label for="newPassword" class="form-label">Password Baru *</label>
            <div class="password-input">
              <input
                id="newPassword"
                v-model="form.newPassword"
                :type="showPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="Masukkan password baru"
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
            <div class="password-input">
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="Ulangi password baru"
                required
              />
              <button 
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="password-toggle"
              >
                {{ showConfirmPassword ? '👁️' : '🙈' }}
              </button>
            </div>
            <div class="password-match" v-if="form.confirmPassword">
              <span v-if="passwordsMatch" class="match-success">✅ Password cocok</span>
              <span v-else class="match-error">❌ Password tidak cocok</span>
            </div>
          </div>

          <div class="form-group">
            <label for="reason" class="form-label">Alasan Reset</label>
            <textarea
              id="reason"
              v-model="form.reason"
              class="form-input"
              rows="3"
              placeholder="Jelaskan alasan reset password (opsional)"
            ></textarea>
          </div>

          <div class="reset-options">
            <h4>Opsi Reset</h4>
            <div class="option-list">
              <label class="option-item">
                <input 
                  type="checkbox" 
                  v-model="form.options.forcePasswordChange"
                />
                <span class="option-text">
                  <strong>Paksa ganti password</strong><br>
                  <small>User harus mengganti password saat login berikutnya</small>
                </span>
              </label>
              
              <label class="option-item">
                <input 
                  type="checkbox" 
                  v-model="form.options.sendEmail"
                />
                <span class="option-text">
                  <strong>Kirim email notifikasi</strong><br>
                  <small>Kirim email ke user tentang perubahan password</small>
                </span>
              </label>
              
              <label class="option-item">
                <input 
                  type="checkbox" 
                  v-model="form.options.logoutAllSessions"
                />
                <span class="option-text">
                  <strong>Logout dari semua perangkat</strong><br>
                  <small>Paksa logout user dari semua sesi aktif</small>
                </span>
              </label>
            </div>
          </div>

          <div class="security-notice">
            <div class="notice-icon">🔒</div>
            <div class="notice-content">
              <h4>Catatan Keamanan</h4>
              <ul>
                <li>Password baru akan menggantikan password lama secara permanen</li>
                <li>User tidak akan bisa menggunakan password lama</li>
                <li>Aktivitas ini akan tercatat dalam log sistem</li>
                <li>Pastikan memberitahu user tentang password baru</li>
              </ul>
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
              <span v-else>🔑 Reset Password</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { User } from '@/types'

interface Props {
  user: User | null
}

interface Emits {
  (e: 'close'): void
  (e: 'reset', password: string, options: any): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const form = reactive({
  newPassword: '',
  confirmPassword: '',
  reason: '',
  options: {
    forcePasswordChange: true,
    sendEmail: true,
    logoutAllSessions: true
  }
})

const passwordStrength = computed(() => {
  const password = form.newPassword
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
    text = `Lemah - tambahkan: ${feedback.slice(0, 2).join(', ')}`
  } else if (score < 60) {
    level = 'fair'
    text = `Cukup - tambahkan: ${feedback.slice(0, 1).join(', ')}`
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
  return form.newPassword === form.confirmPassword
})

const isFormValid = computed(() => {
  return form.newPassword && 
         form.newPassword.length >= 8 && 
         passwordsMatch.value &&
         passwordStrength.value.percentage >= 40
})

const closeModal = () => {
  emit('close')
}

const handleSubmit = async () => {
  if (!isFormValid.value) return
  
  loading.value = true
  
  try {
    emit('reset', form.newPassword, {
      reason: form.reason || undefined,
      ...form.options
    })
  } catch (error) {
    console.error('Error resetting password:', error)
  } finally {
    loading.value = false
  }
}
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
  max-width: 500px;
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

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  margin-bottom: var(--space-6);
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--primary-color);
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-details h3 {
  margin: 0 0 var(--space-1) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
}

.user-email {
  color: var(--text-secondary);
  margin: 0 0 var(--space-2) 0;
  font-size: var(--font-size-sm);
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

.reset-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-label {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.form-input {
  padding: var(--space-3);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--font-size-base);
  transition: all var(--transition-normal);
  resize: vertical;
  min-height: 40px;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
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
  min-width: 100px;
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

.reset-options {
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  border: 1px solid var(--border-color);
}

.reset-options h4 {
  margin: 0 0 var(--space-3) 0;
  color: var(--text-primary);
  font-size: var(--font-size-base);
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  cursor: pointer;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  transition: all var(--transition-normal);
}

.option-item:hover {
  background: var(--bg-primary);
  border-color: var(--border-color);
}

.option-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: var(--primary-color);
  cursor: pointer;
  margin-top: 2px;
}

.option-text {
  flex: 1;
  line-height: 1.4;
}

.option-text strong {
  color: var(--text-primary);
  display: block;
  margin-bottom: var(--space-1);
}

.option-text small {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.security-notice {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
  background: rgba(255, 165, 0, 0.1);
  border: 1px solid rgba(255, 165, 0, 0.3);
  border-radius: var(--radius-lg);
}

.notice-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.notice-content h4 {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.notice-content ul {
  margin: 0;
  padding-left: var(--space-4);
  color: var(--text-secondary);
}

.notice-content li {
  font-size: var(--font-size-xs);
  margin-bottom: var(--space-1);
  line-height: 1.4;
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
  
  .user-info {
    flex-direction: column;
    text-align: center;
  }
  
  .modal-actions {
    flex-direction: column-reverse;
  }
  
  .modal-actions button {
    width: 100%;
  }
}
</style>
