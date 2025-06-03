export interface User {
  id: number;
  username: string;
  email: string;
  role: 'admin' | 'operator' | 'viewer';
  name: string;
  avatar: string;
  created_at: string;
  last_login?: string;
}

export interface Transformer {
  id: number;
  transformer_code: string;
  name: string;
  type: string;
  location_name: string;
  latitude: number;
  longitude: number;
  capacity_kva: number;
  primary_voltage: number;
  secondary_voltage: number;
  installation_date: string;
  last_maintenance_date: string;
  next_maintenance_date: string;
  current_status: TransformerStatus;
  priority_level: number;
  assigned_operator_id: number | null;
  created_at: string;
  updated_at: string;
  health_score: number;
  temperature: number;
  load_percentage: number;
}

export type TransformerStatus = 
  | 'pending' 
  | 'in_progress' 
  | 'testing' 
  | 'completed' 
  | 'maintenance' 
  | 'emergency' 
  | 'offline'
  | 'normal';

export interface StatusHistory {
  id: number;
  transformer_id: number;
  previous_status: TransformerStatus | null;
  new_status: TransformerStatus;
  changed_by: number;
  change_reason: string;
  changed_at: string;
}

export interface WorkOrder {
  id: number;
  transformer_id: number;
  title: string;
  description: string;
  priority: 'low' | 'medium' | 'high' | 'critical';
  status: 'open' | 'in_progress' | 'completed' | 'scheduled';
  assigned_to: number | null;
  created_by: number;
  created_at: string;
  updated_at: string;
  completed_at?: string;
  estimated_completion?: string;
}

export interface DashboardStats {
  total_transformers: number;
  normal: number;
  maintenance: number;
  emergency: number;
  offline: number;
  average_health_score: number;
  active_work_orders: number;
}

export interface NotificationData {
  id: string;
  type: 'success' | 'warning' | 'error' | 'info';
  title: string;
  message: string;
  timestamp: string;
  transformer_id?: number;
}
