<template>
  <div class="settings">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header-left">
        <h1>Pengaturan</h1>
        <p class="page-description">Kelola preferensi dan konfigurasi sistem</p>
      </div>
    </div>

    <!-- Settings Grid -->
    <div class="settings-grid">
      <!-- Theme Settings -->
      <div class="settings-section card">
        <div class="section-header">
          <h3>🎨 Tema & Tampilan</h3>
          <p class="section-description">Kustomisasi tampilan antarmuka</p>
        </div>

        <div class="settings-content">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Mode Gelap</label>
              <p class="setting-description">Menggunakan tema gelap untuk antarmuka</p>
            </div>
            <div class="setting-control">
              <label class="theme-switch">
                <input 
                  type="checkbox" 
                  :checked="themeStore.isDarkMode"
                  @change="themeStore.toggleTheme()"
                />
                <span class="switch-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Warna Primer</label>
              <p class="setting-description">Pilih warna utama untuk interface</p>
            </div>
            <div class="setting-control">
              <div class="color-picker">
                <button 
                  v-for="color in primaryColors"
                  :key="color.value"
                  @click="themeStore.setPrimaryColor(color.value)"
                  class="color-option"
                  :class="{ active: themeStore.primaryColor === color.value }"
                  :style="{ backgroundColor: color.value }"
                  :title="color.name"
                ></button>
              </div>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Warna Aksen</label>
              <p class="setting-description">Pilih warna aksen untuk highlights</p>
            </div>
            <div class="setting-control">
              <div class="color-picker">
                <button 
                  v-for="color in accentColors"
                  :key="color.value"
                  @click="themeStore.setAccentColor(color.value)"
                  class="color-option"
                  :class="{ active: themeStore.accentColor === color.value }"
                  :style="{ backgroundColor: color.value }"
                  :title="color.name"
                ></button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Notification Settings -->
      <div class="settings-section card">
        <div class="section-header">
          <h3>🔔 Notifikasi</h3>
          <p class="section-description">Atur preferensi notifikasi</p>
        </div>

        <div class="settings-content">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Notifikasi Real-time</label>
              <p class="setting-description">Terima notifikasi langsung saat ada perubahan</p>
            </div>
            <div class="setting-control">
              <label class="theme-switch">
                <input 
                  type="checkbox" 
                  v-model="settings.notifications.realtime"
                  @change="saveSettings"
                />
                <span class="switch-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Email Alerts</label>
              <p class="setting-description">Kirim alert penting via email</p>
            </div>
            <div class="setting-control">
              <label class="theme-switch">
                <input 
                  type="checkbox" 
                  v-model="settings.notifications.email"
                  @change="saveSettings"
                />
                <span class="switch-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Sound Alerts</label>
              <p class="setting-description">Mainkan suara untuk notifikasi penting</p>
            </div>
            <div class="setting-control">
              <label class="theme-switch">
                <input 
                  type="checkbox" 
                  v-model="settings.notifications.sound"
                  @change="saveSettings"
                />
                <span class="switch-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Frekuensi Update</label>
              <p class="setting-description">Seberapa sering data diperbarui</p>
            </div>
            <div class="setting-control">
              <select 
                v-model="settings.notifications.updateFrequency"
                @change="saveSettings"
                class="form-select"
              >
                <option value="5">5 detik</option>
                <option value="15">15 detik</option>
                <option value="30">30 detik</option>
                <option value="60">1 menit</option>
                <option value="300">5 menit</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- Dashboard Settings -->
      <div class="settings-section card">
        <div class="section-header">
          <h3>📊 Dashboard</h3>
          <p class="section-description">Kustomisasi tampilan dashboard</p>
        </div>

        <div class="settings-content">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Auto-refresh Dashboard</label>
              <p class="setting-description">Refresh otomatis data dashboard</p>
            </div>
            <div class="setting-control">
              <label class="theme-switch">
                <input 
                  type="checkbox" 
                  v-model="settings.dashboard.autoRefresh"
                  @change="saveSettings"
                />
                <span class="switch-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Items per Page</label>
              <p class="setting-description">Jumlah item yang ditampilkan per halaman</p>
            </div>
            <div class="setting-control">
              <select 
                v-model.number="settings.dashboard.itemsPerPage"
                @change="saveSettings"
                class="form-select"
              >
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
                <option :value="100">100</option>
              </select>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Default Chart Type</label>
              <p class="setting-description">Tipe chart default untuk dashboard</p>
            </div>
            <div class="setting-control">
              <select 
                v-model="settings.dashboard.defaultChartType"
                @change="saveSettings"
                class="form-select"
              >
                <option value="doughnut">Doughnut</option>
                <option value="pie">Pie</option>
                <option value="bar">Bar</option>
                <option value="line">Line</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- Data & Privacy -->
      <div class="settings-section card">
        <div class="section-header">
          <h3>🔒 Data & Privasi</h3>
          <p class="section-description">Pengaturan data dan keamanan</p>
        </div>

        <div class="settings-content">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Session Timeout</label>
              <p class="setting-description">Waktu timeout otomatis untuk sesi login</p>
            </div>
            <div class="setting-control">
              <select 
                v-model.number="settings.security.sessionTimeout"
                @change="saveSettings"
                class="form-select"
              >
                <option :value="30">30 menit</option>
                <option :value="60">1 jam</option>
                <option :value="240">4 jam</option>
                <option :value="480">8 jam</option>
                <option :value="1440">24 jam</option>
              </select>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Data Backup</label>
              <p class="setting-description">Backup otomatis data lokal</p>
            </div>
            <div class="setting-control">
              <label class="theme-switch">
                <input 
                  type="checkbox" 
                  v-model="settings.security.autoBackup"
                  @change="saveSettings"
                />
                <span class="switch-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Clear Cache</label>
              <p class="setting-description">Hapus cache dan data sementara</p>
            </div>
            <div class="setting-control">
              <button 
                class="btn btn-outline btn-sm"
                @click="clearCache"
                :disabled="cacheClearLoading"
              >
                <span v-if="cacheClearLoading" class="spinner"></span>
                <span v-else>🗑️ Clear Cache</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Language & Region -->
      <div class="settings-section card">
        <div class="section-header">
          <h3>🌍 Bahasa & Regional</h3>
          <p class="section-description">Pengaturan bahasa dan format</p>
        </div>

        <div class="settings-content">
          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Bahasa</label>
              <p class="setting-description">Pilih bahasa interface</p>
            </div>
            <div class="setting-control">
              <select 
                v-model="settings.language.locale"
                @change="saveSettings"
                class="form-select"
              >
                <option value="id">Bahasa Indonesia</option>
                <option value="en">English</option>
              </select>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Format Tanggal</label>
              <p class="setting-description">Format tampilan tanggal</p>
            </div>
            <div class="setting-control">
              <select 
                v-model="settings.language.dateFormat"
                @change="saveSettings"
                class="form-select"
              >
                <option value="dd/mm/yyyy">DD/MM/YYYY</option>
                <option value="mm/dd/yyyy">MM/DD/YYYY</option>
                <option value="yyyy-mm-dd">YYYY-MM-DD</option>
              </select>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-info">
              <label class="setting-label">Zona Waktu</label>
              <p class="setting-description">Zona waktu untuk tampilan jam</p>
            </div>
            <div class="setting-control">
              <select 
                v-model="settings.language.timezone"
                @change="saveSettings"
                class="form-select"
              >
                <option value="Asia/Jakarta">WIB (Jakarta)</option>
                <option value="Asia/Makassar">WITA (Makassar)</option>
                <option value="Asia/Jayapura">WIT (Jayapura)</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Settings -->
      <div class="settings-section card">
        <div class="section-header">
          <h3>👤 Profil</h3>
          <p class="section-description">Informasi profil pengguna</p>
        </div>

        <div class="settings-content" v-if="authStore.currentUser">
          <div class="profile-section">
            <div class="profile-avatar-section">
              <img 
                :src="authStore.currentUser.avatar || '/images/admin-avatar.png'"
                :alt="authStore.currentUser.username"
                class="profile-avatar"
              />
              <button class="btn btn-outline btn-sm">
                📷 Change Avatar
              </button>
            </div>

            <div class="profile-info-section">
              <div class="setting-item">
                <div class="setting-info">
                  <label class="setting-label">Username</label>
                </div>
                <div class="setting-control">
                  <input 
                    type="text" 
                    :value="authStore.currentUser.username"
                    readonly
                    class="form-input"
                  />
                </div>
              </div>

              <div class="setting-item">
                <div class="setting-info">
                  <label class="setting-label">Email</label>
                </div>
                <div class="setting-control">
                  <input 
                    type="email" 
                    :value="authStore.currentUser.email"
                    readonly
                    class="form-input"
                  />
                </div>
              </div>

              <div class="setting-item">
                <div class="setting-info">
                  <label class="setting-label">Role</label>
                </div>
                <div class="setting-control">
                  <span 
                    class="role-badge"
                    :class="authStore.currentUser.role"
                  >
                    {{ authStore.currentUser.role }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="settings-actions">
      <button class="btn btn-outline" @click="resetSettings">
        🔄 Reset to Default
      </button>
      <button class="btn btn-secondary" @click="exportSettings">
        📤 Export Settings
      </button>
      <div class="save-status" v-if="saveStatus">
        {{ saveStatus }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const authStore = useAuthStore()
const themeStore = useThemeStore()

const cacheClearLoading = ref(false)
const saveStatus = ref('')

const primaryColors = [
  { name: 'Cyan', value: '#00D4FF' },
  { name: 'Blue', value: '#0066FF' },
  { name: 'Purple', value: '#8B5FBF' },
  { name: 'Pink', value: '#FF6B9D' },
  { name: 'Green', value: '#00FF88' },
  { name: 'Orange', value: '#FF8C42' }
]

const accentColors = [
  { name: 'Green', value: '#00FF88' },
  { name: 'Yellow', value: '#FFD700' },
  { name: 'Orange', value: '#FF8C42' },
  { name: 'Red', value: '#FF4444' },
  { name: 'Pink', value: '#FF6B9D' },
  { name: 'Purple', value: '#8B5FBF' }
]

const settings = reactive({
  notifications: {
    realtime: true,
    email: false,
    sound: true,
    updateFrequency: 30
  },
  dashboard: {
    autoRefresh: true,
    itemsPerPage: 20,
    defaultChartType: 'doughnut'
  },
  security: {
    sessionTimeout: 240,
    autoBackup: true
  },
  language: {
    locale: 'id',
    dateFormat: 'dd/mm/yyyy',
    timezone: 'Asia/Jakarta'
  }
})

const saveSettings = () => {
  // Save to localStorage
  localStorage.setItem('app-settings', JSON.stringify(settings))
  
  // Show save status
  saveStatus.value = '✅ Settings saved'
  setTimeout(() => {
    saveStatus.value = ''
  }, 2000)
}

const loadSettings = () => {
  const saved = localStorage.getItem('app-settings')
  if (saved) {
    Object.assign(settings, JSON.parse(saved))
  }
}

const resetSettings = () => {
  if (confirm('Reset semua pengaturan ke default?')) {
    localStorage.removeItem('app-settings')
    location.reload()
  }
}

const exportSettings = () => {
  const dataStr = JSON.stringify(settings, null, 2)
  const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr)
  
  const exportFileDefaultName = 'pln-monitor-settings.json'
  
  const linkElement = document.createElement('a')
  linkElement.setAttribute('href', dataUri)
  linkElement.setAttribute('download', exportFileDefaultName)
  linkElement.click()
  
  saveStatus.value = '📤 Settings exported'
  setTimeout(() => {
    saveStatus.value = ''
  }, 2000)
}

const clearCache = async () => {
  cacheClearLoading.value = true
  
  try {
    // Clear various types of cache
    localStorage.removeItem('transformer-cache')
    localStorage.removeItem('user-cache')
    
    // Clear any other cached data
    if ('caches' in window) {
      const cacheNames = await caches.keys()
      await Promise.all(
        cacheNames.map(cacheName => caches.delete(cacheName))
      )
    }
    
    saveStatus.value = '🗑️ Cache cleared successfully'
  } catch (error) {
    saveStatus.value = '❌ Error clearing cache'
  } finally {
    cacheClearLoading.value = false
    setTimeout(() => {
      saveStatus.value = ''
    }, 2000)
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--space-8);
}

.page-header h1 {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
}

.page-description {
  color: var(--text-secondary);
  margin: 0;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.settings-section {
  padding: var(--space-6);
}

.section-header {
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.section-header h3 {
  margin: 0 0 var(--space-2) 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
}

.section-description {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
  margin: 0;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.setting-info {
  flex: 1;
  min-width: 0;
}

.setting-label {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
  display: block;
}

.setting-description {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.4;
  margin: 0;
}

.setting-control {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.theme-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
}

.theme-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--bg-primary);
  border: 2px solid var(--border-color);
  transition: all var(--transition-normal);
  border-radius: 26px;
}

.switch-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 2px;
  bottom: 2px;
  background-color: var(--text-muted);
  transition: all var(--transition-normal);
  border-radius: 50%;
}

input:checked + .switch-slider {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: var(--glow-primary);
}

input:checked + .switch-slider:before {
  transform: translateX(24px);
  background-color: white;
}

.color-picker {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 3px solid transparent;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
}

.color-option:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.color-option.active {
  border-color: var(--text-primary);
  box-shadow: 0 0 0 2px var(--bg-secondary), 0 0 0 4px var(--text-primary);
}

.color-option.active::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: bold;
  font-size: 14px;
  text-shadow: 0 0 3px rgba(0, 0, 0, 0.8);
}

.form-select {
  min-width: 150px;
  padding: var(--space-2) var(--space-3);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.form-select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.form-input {
  min-width: 200px;
  padding: var(--space-2) var(--space-3);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all var(--transition-normal);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
}

.form-input:read-only {
  background: var(--bg-tertiary);
  color: var(--text-muted);
}

.profile-section {
  display: flex;
  gap: var(--space-6);
}

.profile-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid var(--primary-color);
  object-fit: cover;
}

.profile-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.role-badge {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  font-weight: 600;
  text-transform: uppercase;
  font-size: var(--font-size-xs);
  letter-spacing: 0.05em;
}

.role-badge.admin {
  background: rgba(255, 68, 68, 0.2);
  color: var(--status-emergency);
}

.role-badge.operator {
  background: rgba(0, 212, 255, 0.2);
  color: var(--primary-color);
}

.role-badge.viewer {
  background: rgba(0, 255, 136, 0.2);
  color: var(--accent-color);
}

.settings-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.save-status {
  color: var(--accent-color);
  font-weight: 600;
  padding: var(--space-2) var(--space-4);
  background: rgba(0, 255, 136, 0.1);
  border-radius: var(--radius-md);
  border: 1px solid rgba(0, 255, 136, 0.3);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border-color);
  border-top: 2px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Responsive Design */
@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-3);
  }
  
  .setting-control {
    justify-content: flex-start;
  }
  
  .profile-section {
    flex-direction: column;
    align-items: center;
  }
  
  .profile-info-section {
    width: 100%;
  }
  
  .settings-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .color-picker {
    justify-content: center;
  }
  
  .form-select,
  .form-input {
    min-width: auto;
    width: 100%;
  }
}
</style>
