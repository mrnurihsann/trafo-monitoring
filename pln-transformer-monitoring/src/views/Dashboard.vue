<template>
  <div class="dashboard">
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card card glow-effect">
        <div class="stat-icon total">📊</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalTransformers }}</div>
          <div class="stat-label">Total Trafo</div>
        </div>
      </div>

      <div class="stat-card card glow-effect">
        <div class="stat-icon active">⚡</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.activeTransformers }}</div>
          <div class="stat-label">Aktif</div>
        </div>
      </div>

      <div class="stat-card card glow-effect">
        <div class="stat-icon maintenance">🔧</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.maintenanceTransformers }}</div>
          <div class="stat-label">Maintenance</div>
        </div>
      </div>

      <div class="stat-card card glow-effect">
        <div class="stat-icon emergency">🚨</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.emergencyTransformers }}</div>
          <div class="stat-label">Emergency</div>
        </div>
      </div>
    </div>

    <!-- Main Dashboard Content -->
    <div class="dashboard-content">
      <!-- Left Column -->
      <div class="dashboard-left">
        <!-- Status Chart -->
        <div class="chart-card card">
          <div class="card-header">
            <h3>Distribusi Status Trafo</h3>
            <div class="chart-controls">
              <button 
                v-for="chartType in chartTypes" 
                :key="chartType.value"
                @click="activeChartType = chartType.value"
                class="chart-btn"
                :class="{ active: activeChartType === chartType.value }"
              >
                {{ chartType.label }}
              </button>
            </div>
          </div>
          <div class="chart-container">
            <canvas ref="statusChartRef" class="chart-canvas"></canvas>
          </div>
        </div>

        <!-- Recent Activities -->
        <div class="activities-card card">
          <div class="card-header">
            <h3>Aktivitas Terbaru</h3>
            <button class="btn btn-ghost btn-sm" @click="refreshActivities">
              🔄 Refresh
            </button>
          </div>
          <div class="activities-list">
            <div 
              v-for="activity in recentActivities" 
              :key="activity.id"
              class="activity-item"
            >
              <div class="activity-avatar">
                <img :src="activity.user?.avatar || '/images/admin-avatar.png'" :alt="activity.user?.username" />
              </div>
              <div class="activity-content">
                <div class="activity-text">
                  <strong>{{ activity.user?.username }}</strong> 
                  mengubah status trafo <strong>{{ activity.transformerCode }}</strong>
                  menjadi <span :class="`status-${activity.newStatus}`">{{ getStatusText(activity.newStatus) }}</span>
                </div>
                <div class="activity-time">{{ formatTime(activity.changedAt) }}</div>
              </div>
            </div>
            <div v-if="recentActivities.length === 0" class="no-activities">
              Belum ada aktivitas terbaru
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div class="dashboard-right">
        <!-- Quick Stats -->
        <div class="quick-stats-card card">
          <div class="card-header">
            <h3>Statistik Hari Ini</h3>
          </div>
          <div class="quick-stats">
            <div class="quick-stat-item">
              <div class="quick-stat-icon completed">✅</div>
              <div class="quick-stat-details">
                <div class="quick-stat-number">{{ stats.completedToday }}</div>
                <div class="quick-stat-label">Selesai Hari Ini</div>
              </div>
            </div>
            <div class="quick-stat-item">
              <div class="quick-stat-icon pending">⏳</div>
              <div class="quick-stat-details">
                <div class="quick-stat-number">{{ stats.pendingWork }}</div>
                <div class="quick-stat-label">Menunggu</div>
              </div>
            </div>
          </div>
        </div>

        <!-- My Assigned Transformers -->
        <div class="assigned-card card" v-if="!authStore.isViewer">
          <div class="card-header">
            <h3>Trafo Saya</h3>
            <router-link to="/transformers" class="btn btn-primary btn-sm">
              Lihat Semua
            </router-link>
          </div>
          <div class="assigned-list">
            <div 
              v-for="transformer in myTransformers.slice(0, 5)" 
              :key="transformer.id"
              class="assigned-item"
              @click="viewTransformer(transformer.id)"
            >
              <div class="assigned-info">
                <div class="assigned-name">{{ transformer.name }}</div>
                <div class="assigned-location">{{ transformer.locationName }}</div>
              </div>
              <div class="assigned-status">
                <span class="status-badge" :class="`status-${transformer.currentStatus}`">
                  {{ getStatusText(transformer.currentStatus) }}
                </span>
                <div class="progress-bar" :style="{ '--progress': transformer.progress + '%' }"></div>
              </div>
            </div>
            <div v-if="myTransformers.length === 0" class="no-assignments">
              Belum ada trafo yang ditugaskan
            </div>
          </div>
        </div>

        <!-- Performance Chart -->
        <div class="performance-card card">
          <div class="card-header">
            <h3>Performa 7 Hari Terakhir</h3>
          </div>
          <div class="performance-chart">
            <canvas ref="performanceChartRef" class="chart-canvas"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTransformerStore } from '@/stores/transformers'
import type { StatusHistory, Transformer, TransformerStatus } from '@/types'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const router = useRouter()
const authStore = useAuthStore()
const transformerStore = useTransformerStore()

const statusChartRef = ref<HTMLCanvasElement>()
const performanceChartRef = ref<HTMLCanvasElement>()
const activeChartType = ref<'pie' | 'doughnut' | 'bar'>('doughnut')

const chartTypes = [
  { value: 'doughnut', label: 'Donut' },
  { value: 'pie', label: 'Pie' },
  { value: 'bar', label: 'Bar' }
]

let statusChart: Chart | null = null
let performanceChart: Chart | null = null

const stats = computed(() => transformerStore.dashboardStats)
const myTransformers = computed(() => transformerStore.myAssignedTransformers)

const recentActivities = ref<(StatusHistory & { transformerCode?: string })[]>([])

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
  const hours = Math.floor(diff / 3600000)
  
  if (hours < 1) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes} menit yang lalu`
  }
  if (hours < 24) return `${hours} jam yang lalu`
  
  const days = Math.floor(hours / 24)
  return `${days} hari yang lalu`
}

const viewTransformer = (id: number) => {
  router.push(`/transformers/${id}`)
}

const refreshActivities = async () => {
  await loadData()
}

const createStatusChart = () => {
  if (!statusChartRef.value) return

  const ctx = statusChartRef.value.getContext('2d')
  if (!ctx) return

  const statusData = transformerStore.transformersByStatus
  const labels = Object.keys(statusData).map(status => getStatusText(status as TransformerStatus))
  const data = Object.values(statusData)
  const colors = [
    '#FFA500', // pending
    '#00D4FF', // in-progress  
    '#8B5FBF', // testing
    '#00FF88', // completed
    '#FFD700', // maintenance
    '#FF4444', // emergency
    '#666666'  // offline
  ]

  if (statusChart) {
    statusChart.destroy()
  }

  statusChart = new Chart(ctx, {
    type: activeChartType.value,
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: colors,
        borderColor: colors.map(color => color + '80'),
        borderWidth: 2,
        hoverBorderWidth: 3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 20,
            usePointStyle: true,
            color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary')
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: '#fff',
          bodyColor: '#fff',
          borderColor: '#00D4FF',
          borderWidth: 1
        }
      },
      ...(activeChartType.value === 'bar' && {
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: getComputedStyle(document.documentElement).getPropertyValue('--border-color')
            },
            ticks: {
              color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary')
            }
          },
          x: {
            grid: {
              color: getComputedStyle(document.documentElement).getPropertyValue('--border-color')
            },
            ticks: {
              color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary')
            }
          }
        }
      })
    }
  })
}

const createPerformanceChart = () => {
  if (!performanceChartRef.value) return

  const ctx = performanceChartRef.value.getContext('2d')
  if (!ctx) return

  // Mock performance data for the last 7 days
  const labels = []
  const completedData = []
  const startedData = []
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    labels.push(date.toLocaleDateString('id-ID', { weekday: 'short', day: 'numeric' }))
    completedData.push(Math.floor(Math.random() * 10) + 5)
    startedData.push(Math.floor(Math.random() * 15) + 8)
  }

  if (performanceChart) {
    performanceChart.destroy()
  }

  performanceChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: 'Selesai',
          data: completedData,
          borderColor: '#00FF88',
          backgroundColor: 'rgba(0, 255, 136, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#00FF88',
          pointBorderColor: '#00FF88',
          pointHoverRadius: 8
        },
        {
          label: 'Dimulai',
          data: startedData,
          borderColor: '#00D4FF',
          backgroundColor: 'rgba(0, 212, 255, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#00D4FF',
          pointBorderColor: '#00D4FF',
          pointHoverRadius: 8
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          labels: {
            padding: 20,
            usePointStyle: true,
            color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary')
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: '#fff',
          bodyColor: '#fff',
          borderColor: '#00D4FF',
          borderWidth: 1
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: {
            color: getComputedStyle(document.documentElement).getPropertyValue('--border-color')
          },
          ticks: {
            color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary')
          }
        },
        x: {
          grid: {
            color: getComputedStyle(document.documentElement).getPropertyValue('--border-color')
          },
          ticks: {
            color: getComputedStyle(document.documentElement).getPropertyValue('--text-secondary')
          }
        }
      },
      interaction: {
        intersect: false,
        mode: 'index'
      }
    }
  })
}

const loadData = async () => {
  try {
    await Promise.all([
      transformerStore.fetchTransformers(),
      transformerStore.fetchStatusHistory()
    ])

    // Load recent activities with transformer data
    const activities = transformerStore.statusHistory.slice(0, 10)
    recentActivities.value = activities.map(activity => {
      const transformer = transformerStore.getTransformerById(activity.transformerId)
      return {
        ...activity,
        transformerCode: transformer?.transformerCode || `TRF${activity.transformerId.toString().padStart(3, '0')}`
      }
    })
  } catch (error) {
    console.error('Error loading dashboard data:', error)
  }
}

onMounted(async () => {
  await loadData()
  
  nextTick(() => {
    createStatusChart()
    createPerformanceChart()
  })
})

// Watch for chart type changes
import { watch } from 'vue'
watch(activeChartType, () => {
  nextTick(() => {
    createStatusChart()
  })
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
  background: linear-gradient(135deg, var(--bg-secondary), var(--bg-tertiary));
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
  box-shadow: var(--glow-primary);
}

.stat-icon.total {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.stat-icon.active {
  background: linear-gradient(135deg, var(--primary-color), #0099cc);
}

.stat-icon.maintenance {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-icon.emergency {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: var(--font-size-3xl);
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
  letter-spacing: 0.05em;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-8);
}

.dashboard-left,
.dashboard-right {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.card-header {
  display: flex;
  justify-content: between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.card-header h3 {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-primary);
}

.chart-controls {
  display: flex;
  gap: var(--space-2);
}

.chart-btn {
  padding: var(--space-2) var(--space-3);
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: var(--font-size-xs);
  font-weight: 500;
  transition: all var(--transition-normal);
}

.chart-btn:hover,
.chart-btn.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--glow-primary);
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart-canvas {
  max-height: 100%;
}

.activities-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  transition: background var(--transition-normal);
}

.activity-item:hover {
  background: var(--bg-tertiary);
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--primary-color);
  flex-shrink: 0;
}

.activity-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.5;
  margin-bottom: var(--space-1);
}

.activity-text strong {
  color: var(--text-primary);
}

.activity-time {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.no-activities {
  padding: var(--space-8);
  text-align: center;
  color: var(--text-muted);
}

.quick-stats {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.quick-stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.quick-stat-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.quick-stat-icon.completed {
  background: rgba(0, 255, 136, 0.2);
}

.quick-stat-icon.pending {
  background: rgba(255, 165, 0, 0.2);
}

.quick-stat-details {
  flex: 1;
}

.quick-stat-number {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.quick-stat-label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.assigned-list {
  max-height: 300px;
  overflow-y: auto;
}

.assigned-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.assigned-item:hover {
  background: var(--bg-tertiary);
  transform: translateX(5px);
}

.assigned-item:last-child {
  border-bottom: none;
}

.assigned-info {
  flex: 1;
  min-width: 0;
}

.assigned-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.assigned-location {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.assigned-status {
  text-align: right;
  min-width: 0;
}

.performance-chart {
  height: 250px;
  position: relative;
}

.no-assignments {
  padding: var(--space-8);
  text-align: center;
  color: var(--text-muted);
}

/* Status Badge Styles */
.status-pending { color: var(--status-pending); }
.status-in-progress { color: var(--status-in-progress); }
.status-testing { color: var(--status-testing); }
.status-completed { color: var(--status-completed); }
.status-maintenance { color: var(--status-maintenance); }
.status-emergency { color: var(--status-emergency); }
.status-offline { color: var(--status-offline); }

/* Responsive Design */
@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }
  
  .stat-card {
    padding: var(--space-4);
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
  
  .stat-number {
    font-size: var(--font-size-2xl);
  }
  
  .chart-controls {
    flex-wrap: wrap;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>
