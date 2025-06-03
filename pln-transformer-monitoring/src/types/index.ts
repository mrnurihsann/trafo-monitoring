export interface User {
  id: number
  username: string
  email: string
  role: 'admin' | 'operator' | 'viewer'
  avatar?: string
  createdAt: string
  updatedAt: string
}

export interface Transformer {
  id: number
  transformerCode: string
  name: string
  type: string
  locationName: string
  latitude?: number
  longitude?: number
  capacityKva: number
  primaryVoltage: number
  secondaryVoltage: number
  installationDate?: string
  lastMaintenanceDate?: string
  nextMaintenanceDate?: string
  currentStatus: TransformerStatus
  priorityLevel: number
  assignedOperatorId?: number
  assignedOperator?: User
  progress: number
  notes?: string
  createdAt: string
  updatedAt: string
}

export type TransformerStatus = 
  | 'pending'
  | 'in-progress' 
  | 'testing'
  | 'completed'
  | 'maintenance'
  | 'emergency'
  | 'offline'

export interface StatusHistory {
  id: number
  transformerId: number
  previousStatus?: TransformerStatus
  newStatus: TransformerStatus
  changedBy: number
  changeReason?: string
  changedAt: string
  user?: User
}

export interface WorkOrder {
  id: number
  transformerId: number
  title: string
  description?: string
  priority: 'low' | 'medium' | 'high' | 'critical'
  status: 'open' | 'in-progress' | 'completed' | 'cancelled'
  assignedTo?: number
  createdBy: number
  createdAt: string
  updatedAt: string
  completedAt?: string
  transformer?: Transformer
  assignedOperator?: User
  createdByUser?: User
}

export interface Notification {
  id: number
  userId: number
  title: string
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
  isRead: boolean
  createdAt: string
}

export interface DashboardStats {
  totalTransformers: number
  activeTransformers: number
  maintenanceTransformers: number
  emergencyTransformers: number
  completedToday: number
  pendingWork: number
}

export interface ChartData {
  labels: string[]
  datasets: {
    label: string
    data: number[]
    backgroundColor?: string | string[]
    borderColor?: string | string[]
    borderWidth?: number
  }[]
}

export interface ThemeConfig {
  isDark: boolean
  primaryColor: string
  accentColor: string
}
