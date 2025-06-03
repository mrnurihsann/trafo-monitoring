<template>
  <div class="reports">
    <!-- Page Header -->
    <div class="page-header">
      <div class="page-header-left">
        <h1>Laporan & Analytics</h1>
        <p class="page-description">Analisis data dan laporan performa trafo PLN</p>
      </div>
      <div class="page-header-right">
        <button class="btn btn-secondary" @click="exportReport">
          📊 Export PDF
        </button>
        <button class="btn btn-primary" @click="refreshData">
          🔄 Refresh
        </button>
      </div>
    </div>

    <!-- Filter Controls -->
    <div class="filter-controls card">
      <div class="filter-row">
        <div class="filter-group">
          <label class="filter-label">Periode:</label>
          <select v-model="selectedPeriod" class="form-select">
            <option value="7">7 Hari Terakhir</option>
            <option value="30">30 Hari Terakhir</option>
            <option value="90">3 Bulan Terakhir</option>
            <option value="365">1 Tahun Terakhir</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label class="filter-label">Status:</label>
          <select v-model="selectedStatus" class="form-select">
            <option value="">Semua Status</option>
            <option value="completed">Selesai</option>
            <option value="in-progress">Dikerjakan</option>
            <option value="maintenance">Maintenance</option>
            <option value="emergency">Emergency</option>
          </select>
        </div>

        <div class="filter-group">
          <label class="filter-label">Prioritas:</label>
          <select v-model="selectedPriority" class="form-select">
            <option value="">Semua Prioritas</option>
            <option value="5">Emergency</option>
            <option value="4">Kritis</option>
            <option value="3">Tinggi</option>
            <option value="2">Normal</option>
            <option value="1">Rendah</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Summary Cards -->
    <div class="summary-grid">
      <div class="summary-card card glow-effect">
        <div class="summary-icon completed">✅</div>
        <div class="summary-content">
          <div class="summary-number">{{ summaryData.completed }}</div>
          <div class="summary-label">Selesai</div>
          <div class="summary-change positive">+{{ summaryData.completedChange }}%</div>
        </div>
      </div>

      <div class="summary-card card glow-effect">
        <div class="summary-icon active">⚡</div>
        <div class="summary-content">
          <div class="summary-number">{{ summaryData.active }}</div>
          <div class="summary-label">Aktif</div>
          <div class="summary-change">{{ summaryData.activeChange }}%</div>
        </div>
      </div>

      <div class="summary-card card glow-effect">
        <div class="summary-icon maintenance">🔧</div>
        <div class="summary-content">
          <div class="summary-number">{{ summaryData.maintenance }}</div>
          <div class="summary-label">Maintenance</div>
          <div class="summary-change negative">{{ summaryData.maintenanceChange }}%</div>
        </div>
      </div>

      <div class="summary-card card glow-effect">
        <div class="summary-icon efficiency">📈</div>
        <div class="summary-content">
          <div class="summary-number">{{ summaryData.efficiency }}%</div>
          <div class="summary-label">Efisiensi</div>
          <div class="summary-change positive">+{{ summaryData.efficiencyChange }}%</div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <!-- Performance Trend Chart -->
      <div class="chart-card card">
        <div class="chart-header">
          <h3>Tren Performa</h3>
          <div class="chart-controls">
            <button 
              v-for="chartPeriod in chartPeriods"
              :key="chartPeriod.value"
              @click="activeChartPeriod = chartPeriod.value"
              class="chart-btn"
              :class="{ active: activeChartPeriod === chartPeriod.value }"
            >
              {{ chartPeriod.label }}
            </button>
          </div>
        </div>
        <div class="chart-container">
          <canvas ref="trendChartRef" class="chart-canvas"></canvas>
        </div>
      </div>

      <!-- Status Distribution -->
      <div class="chart-card card">
        <div class="chart-header">
          <h3>Distribusi Status</h3>
        </div>
        <div class="chart-container">
          <canvas ref="statusChartRef" class="chart-canvas"></canvas>
        </div>
      </div>
    </div>

    <!-- Detailed Reports -->
    <div class="reports-section">
      <!-- Top Performers -->
      <div class="report-card card">
        <div class="card-header">
          <h3>Top Performers</h3>
          <span class="card-subtitle">Operator dengan performa terbaik</span>
        </div>
        <div class="performers-list">
          <div 
            v-for="(performer, index) in topPerformers" 
            :key="performer.id"
            class="performer-item"
          >
            <div class="performer-rank">{{ index + 1 }}</div>
            <div class="performer-info">
              <img 
                :src="performer.avatar || '/images/admin-avatar.png'" 
                :alt="performer.username"
                class="performer-avatar"
              />
              <div class="performer-details">
                <div class="performer-name">{{ performer.username }}</div>
                <div class="performer-role">{{ performer.role }}</div>
              </div>
            </div>
            <div class="performer-stats">
              <div class="stat-item">
                <span class="stat-value">{{ performer.completed }}</span>
                <span class="stat-label">Selesai</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ performer.efficiency }}%</span>
                <span class="stat-label">Efisiensi</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Critical Issues -->
      <div class="report-card card">
        <div class="card-header">
          <h3>Isu Kritis</h3>
          <span class="card-subtitle">Trafo yang memerlukan perhatian</span>
        </div>
        <div class="issues-list">
          <div 
            v-for="issue in criticalIssues" 
            :key="issue.id"
            class="issue-item"
            :class="issue.severity"
          >
            <div class="issue-icon">
              {{ issue.icon }}
            </div>
            <div class="issue-content">
              <div class="issue-title">{{ issue.title }}</div>
              <div class="issue-description">{{ issue.description }}</div>
              <div class="issue-meta">
                <span class="issue-transformer">{{ issue.transformerCode }}</span>
                <span class="issue-time">{{ formatTime(issue.createdAt) }}</span>
              </div>
            </div>
            <div class="issue-actions">
              <button class="btn btn-sm btn-outline" @click="viewIssue(issue)">
                Lihat
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Data Table -->
    <div class="table-section card">
      <div class="card-header">
        <h3>Detail Data Trafo</h3>
        <div class="table-controls">
          <input
            v-model="tableSearch"
            type="text"
            placeholder="Cari..."
            class="form-input table-search"
          />
          <button class="btn btn-ghost btn-sm" @click="exportTable">
            📋 Export Excel
          </button>
        </div>
      </div>
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th @click="sortTable('transformerCode')">
                Kode Trafo
                <span class="sort-icon" v-if="sortField === 'transformerCode'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortTable('name')">
                Nama
                <span class="sort-icon" v-if="sortField === 'name'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortTable('currentStatus')">
                Status
                <span class="sort-icon" v-if="sortField === 'currentStatus'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortTable('progress')">
                Progress
                <span class="sort-icon" v-if="sortField === 'progress'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortTable('priorityLevel')">
                Prioritas
                <span class="sort-icon" v-if="sortField === 'priorityLevel'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortTable('updatedAt')">
                Update Terakhir
                <span class="sort-icon" v-if="sortField === 'updatedAt'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="transformer in paginatedTableData" 
              :key="transformer.id"
              @click="viewTransformer(transformer.id)"
              class="table-row"
            >
              <td>
                <span class="transformer-code">{{ transformer.transformerCode }}</span>
              </td>
              <td>{{ transformer.name }}</td>
              <td>
                <span 
                  class="status-badge" 
                  :class="`status-${transformer.currentStatus}`"
                >
                  {{ getStatusText(transformer.currentStatus) }}
                </span>
              </td>
              <td>
                <div class="progress-cell">
                  <div class="progress-bar mini">
                    <div 
                      class="progress-fill" 
                      :style="{ 
                        width: transformer.progress + '%',
                        backgroundColor: themeStore.getStatusColor(transformer.currentStatus)
                      }"
                    ></div>
                  </div>
                  <span class="progress-text">{{ transformer.progress }}%</span>
                </div>
              </td>
              <td>
                <span 
                  class="priority-badge" 
                  :style="{ backgroundColor: themeStore.getPriorityColor(transformer.priorityLevel) }"
                >
                  {{ transformer.priorityLevel }}
                </span>
              </td>
              <td>{{ formatDate(transformer.updatedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination -->
      <div class="table-pagination" v-if="totalTablePages > 1">
        <button 
          class="btn btn-ghost btn-sm"
          @click="currentTablePage = Math.max(1, currentTablePage - 1)"
          :disabled="currentTablePage === 1"
        >
          ← Prev
        </button>
        
        <span class="pagination-info">
          {{ currentTablePage }} / {{ totalTablePages }}
        </span>
        
        <button 
          class="btn btn-ghost btn-sm"
          @click="currentTablePage = Math.min(totalTablePages, currentTablePage + 1)"
          :disabled="currentTablePage === totalTablePages"
        >
          Next →
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTransformerStore } from '@/stores/transformers'
import { useThemeStore } from '@/stores/theme'
import type { Transformer, TransformerStatus, User } from '@/types'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const router = useRouter()
const transformerStore = useTransformerStore()
const themeStore = useThemeStore()

// Refs for charts
const trendChartRef = ref<HTMLCanvasElement>()
const statusChartRef = ref<HTMLCanvasElement>()

// Chart instances
let trendChart: Chart | null = null
let statusChart: Chart | null = null

// Filter states
const selectedPeriod = ref('30')
const selectedStatus = ref('')
const selectedPriority = ref('')
const activeChartPeriod = ref('7d')

// Table states
const tableSearch = ref('')
const sortField = ref('updatedAt')
const sortDirection = ref<'asc' | 'desc'>('desc')
const currentTablePage = ref(1)
const tableItemsPerPage = 10

const chartPeriods = [
  { value: '7d', label: '7D' },
  { value: '1m', label: '1M' },
  { value: '3m', label: '3M' },
  { value: '6m', label: '6M' }
]

// Mock data
const summaryData = ref({
  completed: 128,
  completedChange: 12,
  active: 45,
  activeChange: -3,
  maintenance: 23,
  maintenanceChange: -8,
  efficiency: 94,
  efficiencyChange: 5
})

const topPerformers = ref([
  {
    id: 1,
    username: 'operator1',
    role: 'operator',
    avatar: '/images/operator1-avatar.png',
    completed: 42,
    efficiency: 98
  },
  {
    id: 2,
    username: 'operator2',
    role: 'operator',
    avatar: '/images/operator2-avatar.png',
    completed: 38,
    efficiency: 95
  },
  {
    id: 3,
    username: 'operator3',
    role: 'operator',
    avatar: '/images/operator3-avatar.png',
    completed: 35,
    efficiency: 92
  }
])

const criticalIssues = ref([
  {
    id: 1,
    title: 'Trafo TRF001 Emergency',
    description: 'Ditemukan masalah serius pada sistem cooling',
    transformerCode: 'TRF001',
    severity: 'critical',
    icon: '🚨',
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    title: 'Maintenance Overdue',
    description: 'TRF005 sudah melewati jadwal maintenance 2 minggu',
    transformerCode: 'TRF005',
    severity: 'warning',
    icon: '⚠️',
    createdAt: new Date(Date.now() - 86400000).toISOString()
  }
])

// Computed properties
const filteredTableData = computed(() => {
  let filtered = [...transformerStore.transformers]

  // Apply search
  if (tableSearch.value) {
    const query = tableSearch.value.toLowerCase()
    filtered = filtered.filter(t => 
      t.name.toLowerCase().includes(query) ||
      t.transformerCode.toLowerCase().includes(query)
    )
  }

  // Apply sorting
  filtered.sort((a, b) => {
    let aValue = a[sortField.value as keyof Transformer]
    let bValue = b[sortField.value as keyof Transformer]

    if (typeof aValue === 'string') {
      aValue = aValue.toLowerCase()
      bValue = (bValue as string).toLowerCase()
    }

    if (sortDirection.value === 'asc') {
      return aValue < bValue ? -1 : aValue > bValue ? 1 : 0
    } else {
      return aValue > bValue ? -1 : aValue < bValue ? 1 : 0
    }
  })

  return filtered
})

const paginatedTableData = computed(() => {
  const start = (currentTablePage.value - 1) * tableItemsPerPage
  const end = start + tableItemsPerPage
  return filteredTableData.value.slice(start, end)
})

const totalTablePages = computed(() => 
  Math.ceil(filteredTableData.value.length / tableItemsPerPage)
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
  const hours = Math.floor(diff / 3600000)
  
  if (hours < 1) return 'Baru saja'
  if (hours < 24) return `${hours} jam lalu`
  
  const days = Math.floor(hours / 24)
  return `${days} hari lalu`
}

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('id-ID', {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit'
  })
}

const sortTable = (field: string) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'desc'
  }
  currentTablePage.value = 1
}

const viewTransformer = (id: number) => {
  router.push(`/transformers/${id}`)
}

const viewIssue = (issue: any) => {
  // Navigate to transformer detail or show issue modal
  console.log('View issue:', issue)
}

const refreshData = async () => {
  await transformerStore.fetchTransformers()
  createCharts()
}

const exportReport = () => {
  // Mock export functionality
  console.log('Exporting PDF report...')
  alert('Laporan PDF akan diunduh (fitur demo)')
}

const exportTable = () => {
  // Mock export functionality
  console.log('Exporting Excel table...')
  alert('Data Excel akan diunduh (fitur demo)')
}

const createTrendChart = () => {
  if (!trendChartRef.value) return

  const ctx = trendChartRef.value.getContext('2d')
  if (!ctx) return

  // Mock trend data
  const labels = []
  const completedData = []
  const activeData = []
  
  const days = activeChartPeriod.value === '7d' ? 7 : 30
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    labels.push(date.toLocaleDateString('id-ID', { 
      day: 'numeric', 
      month: 'short' 
    }))
    completedData.push(Math.floor(Math.random() * 10) + 5)
    activeData.push(Math.floor(Math.random() * 15) + 8)
  }

  if (trendChart) {
    trendChart.destroy()
  }

  trendChart = new Chart(ctx, {
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
          tension: 0.4
        },
        {
          label: 'Aktif',
          data: activeData,
          borderColor: '#00D4FF',
          backgroundColor: 'rgba(0, 212, 255, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4
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
            color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary'),
            usePointStyle: true
          }
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
      }
    }
  })
}

const createStatusChart = () => {
  if (!statusChartRef.value) return

  const ctx = statusChartRef.value.getContext('2d')
  if (!ctx) return

  const statusData = transformerStore.transformersByStatus
  const labels = Object.keys(statusData).map(status => getStatusText(status as TransformerStatus))
  const data = Object.values(statusData)
  const colors = [
    '#FFA500', '#00D4FF', '#8B5FBF', '#00FF88', 
    '#FFD700', '#FF4444', '#666666'
  ]

  if (statusChart) {
    statusChart.destroy()
  }

  statusChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: colors,
        borderColor: colors.map(color => color + '80'),
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: getComputedStyle(document.documentElement).getPropertyValue('--text-primary'),
            usePointStyle: true,
            padding: 20
          }
        }
      }
    }
  })
}

const createCharts = () => {
  nextTick(() => {
    createTrendChart()
    createStatusChart()
  })
}

// Watch for chart period changes
watch(activeChartPeriod, () => {
  createTrendChart()
})

onMounted(async () => {
  await transformerStore.fetchTransformers()
  createCharts()
})
</script>

<style scoped>
.reports {
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

.filter-controls {
  padding: var(--space-6);
  margin-bottom: var(--space-8);
}

.filter-row {
  display: flex;
  gap: var(--space-6);
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.filter-label {
  font-weight: 500;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.summary-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
}

.summary-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.summary-icon.completed { background: linear-gradient(135deg, #00FF88, #00cc66); }
.summary-icon.active { background: linear-gradient(135deg, #00D4FF, #0099cc); }
.summary-icon.maintenance { background: linear-gradient(135deg, #FFD700, #cc8800); }
.summary-icon.efficiency { background: linear-gradient(135deg, #8B5FBF, #6d4c99); }

.summary-content {
  flex: 1;
}

.summary-number {
  font-size: var(--font-size-3xl);
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: var(--space-1);
}

.summary-label {
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: var(--space-1);
}

.summary-change {
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.summary-change.positive { color: var(--accent-color); }
.summary-change.negative { color: var(--status-emergency); }

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-8);
  margin-bottom: var(--space-8);
}

.chart-card {
  padding: var(--space-6);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.chart-header h3 {
  margin: 0;
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
  transition: all var(--transition-normal);
}

.chart-btn:hover,
.chart-btn.active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.chart-container {
  height: 300px;
  position: relative;
}

.reports-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-8);
  margin-bottom: var(--space-8);
}

.report-card {
  padding: var(--space-6);
}

.card-header {
  margin-bottom: var(--space-6);
}

.card-header h3 {
  margin: 0 0 var(--space-1) 0;
  color: var(--text-primary);
}

.card-subtitle {
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.performers-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.performer-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.performer-rank {
  width: 32px;
  height: 32px;
  background: var(--primary-color);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.performer-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
}

.performer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid var(--primary-color);
}

.performer-name {
  font-weight: 600;
  color: var(--text-primary);
}

.performer-role {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  text-transform: capitalize;
}

.performer-stats {
  display: flex;
  gap: var(--space-4);
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.issues-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.issue-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.issue-item.critical {
  background: rgba(255, 68, 68, 0.1);
  border-color: var(--status-emergency);
}

.issue-item.warning {
  background: rgba(255, 165, 0, 0.1);
  border-color: var(--status-pending);
}

.issue-icon {
  font-size: 20px;
  margin-top: 2px;
}

.issue-content {
  flex: 1;
  min-width: 0;
}

.issue-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.issue-description {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-2);
}

.issue-meta {
  display: flex;
  gap: var(--space-3);
}

.issue-transformer {
  font-family: var(--font-mono);
  color: var(--primary-color);
  font-weight: 600;
  font-size: var(--font-size-xs);
}

.issue-time {
  color: var(--text-muted);
  font-size: var(--font-size-xs);
}

.table-section {
  padding: var(--space-6);
}

.table-controls {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.table-search {
  max-width: 300px;
}

.table-container {
  overflow-x: auto;
  margin: var(--space-6) 0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: var(--bg-tertiary);
  padding: var(--space-4);
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 2px solid var(--border-color);
  cursor: pointer;
  user-select: none;
  transition: background var(--transition-normal);
}

.data-table th:hover {
  background: rgba(0, 212, 255, 0.1);
}

.data-table td {
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.table-row {
  cursor: pointer;
  transition: background var(--transition-normal);
}

.table-row:hover {
  background: var(--bg-tertiary);
}

.sort-icon {
  margin-left: var(--space-2);
  color: var(--primary-color);
}

.transformer-code {
  font-family: var(--font-mono);
  background: rgba(0, 212, 255, 0.1);
  color: var(--primary-color);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: var(--font-size-xs);
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.progress-bar.mini {
  width: 60px;
  height: 6px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.progress-text {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.priority-badge {
  color: white;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.table-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-color);
}

.pagination-info {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .charts-section,
  .reports-section {
    grid-template-columns: 1fr;
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
  
  .filter-row {
    flex-direction: column;
    gap: var(--space-4);
  }
  
  .summary-grid {
    grid-template-columns: 1fr;
  }
  
  .table-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .table-search {
    max-width: none;
  }
}
</style>
