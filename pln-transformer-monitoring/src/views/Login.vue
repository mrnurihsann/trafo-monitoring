<template>
  <div class="login-container">
    <div class="login-background"></div>
    <div class="login-content">
      <div class="login-card card-glass">
        <div class="login-header">
          <div class="logo">
            <div class="logo-icon">⚡</div>
            <h1>PLN Monitor</h1>
          </div>
          <p class="login-subtitle">Sistem Monitoring Trafo PLN</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="username" class="form-label">Username</label>
            <input
              id="username"
              v-model="credentials.username"
              type="text"
              class="form-input"
              placeholder="Masukkan username"
              required
            />
          </div>

          <div class="form-group">
            <label for="password" class="form-label">Password</label>
            <input
              id="password"
              v-model="credentials.password"
              type="password"
              class="form-input"
              placeholder="Masukkan password"
              required
            />
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="rememberMe" />
              <span class="checkmark"></span>
              Ingat saya
            </label>
          </div>

          <button
            type="submit"
            class="btn btn-primary w-full login-btn"
            :disabled="loading"
          >
            <span v-if="loading" class="spinner"></span>
            <span v-else>Masuk</span>
          </button>

          <div v-if="error" class="error-message">
            {{ error }}
          </div>
        </form>

        <div class="demo-accounts">
          <h3>Demo Accounts:</h3>
          <div class="demo-list">
            <div class="demo-item" @click="loginAs('admin')">
              <span class="demo-role admin">Admin</span>
              <span class="demo-username">admin</span>
            </div>
            <div class="demo-item" @click="loginAs('operator1')">
              <span class="demo-role operator">Operator</span>
              <span class="demo-username">operator1</span>
            </div>
            <div class="demo-item" @click="loginAs('viewer1')">
              <span class="demo-role viewer">Viewer</span>
              <span class="demo-username">viewer1</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const credentials = ref({
  username: '',
  password: ''
})

const rememberMe = ref(false)
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  if (!credentials.value.username || !credentials.value.password) {
    error.value = 'Username dan password harus diisi'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await authStore.login(credentials.value.username, credentials.value.password)
    router.push('/dashboard')
  } catch (err: any) {
    error.value = err.message || 'Login gagal'
  } finally {
    loading.value = false
  }
}

const loginAs = (username: string) => {
  credentials.value.username = username
  credentials.value.password = 'password123'
  handleLogin()
}

onMounted(() => {
  themeStore.loadThemePreference()
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: var(--space-4);
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  z-index: -2;
}

.login-background::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(0, 212, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(0, 255, 136, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(139, 95, 191, 0.1) 0%, transparent 50%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

.login-content {
  width: 100%;
  max-width: 450px;
}

.login-card {
  padding: var(--space-8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color), var(--purple-accent));
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.logo-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: var(--glow-primary);
}

.login-header h1 {
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: var(--font-size-3xl);
  font-weight: 800;
  margin: 0;
}

.login-subtitle {
  color: var(--text-muted);
  font-size: var(--font-size-lg);
  margin: 0;
}

.login-form {
  margin-bottom: var(--space-8);
}

.form-group {
  margin-bottom: var(--space-6);
}

.form-label {
  display: block;
  font-weight: 500;
  margin-bottom: var(--space-2);
  color: var(--text-primary);
}

.form-input {
  width: 100%;
  padding: var(--space-4);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font-size: var(--font-size-base);
  transition: all var(--transition-normal);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
  background: rgba(255, 255, 255, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.checkbox-label input[type="checkbox"] {
  appearance: none;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
  position: relative;
  transition: all var(--transition-normal);
}

.checkbox-label input[type="checkbox"]:checked {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.checkbox-label input[type="checkbox"]:checked::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.login-btn {
  height: 50px;
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--space-4);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  background: rgba(255, 68, 68, 0.1);
  color: var(--status-emergency);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 68, 68, 0.3);
  text-align: center;
  font-size: var(--font-size-sm);
}

.demo-accounts {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: var(--space-6);
}

.demo-accounts h3 {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-4);
  text-align: center;
}

.demo-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.demo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.demo-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--primary-color);
}

.demo-role {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
  text-transform: uppercase;
}

.demo-role.admin {
  background: rgba(255, 68, 68, 0.2);
  color: var(--status-emergency);
}

.demo-role.operator {
  background: rgba(0, 212, 255, 0.2);
  color: var(--primary-color);
}

.demo-role.viewer {
  background: rgba(0, 255, 136, 0.2);
  color: var(--accent-color);
}

.demo-username {
  color: var(--text-secondary);
  font-family: var(--font-mono);
  font-size: var(--font-size-sm);
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@media (max-width: 768px) {
  .login-container {
    padding: var(--space-2);
  }
  
  .login-card {
    padding: var(--space-6);
  }
  
  .logo {
    flex-direction: column;
    gap: var(--space-2);
  }
  
  .logo-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .login-header h1 {
    font-size: var(--font-size-2xl);
  }
}
</style>
