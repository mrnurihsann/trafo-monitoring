import { User, Transformer, StatusHistory, WorkOrder, DashboardStats } from '@/types';

class ApiService {
  private baseUrl = '';

  async fetchUsers(): Promise<User[]> {
    try {
      const response = await fetch('/data/users.json');
      if (!response.ok) throw new Error('Failed to fetch users');
      return await response.json();
    } catch (error) {
      console.error('Error fetching users:', error);
      return [];
    }
  }

  async fetchTransformers(): Promise<Transformer[]> {
    try {
      const response = await fetch('/data/transformers.json');
      if (!response.ok) throw new Error('Failed to fetch transformers');
      return await response.json();
    } catch (error) {
      console.error('Error fetching transformers:', error);
      return [];
    }
  }

  async fetchStatusHistory(): Promise<StatusHistory[]> {
    try {
      const response = await fetch('/data/status_history.json');
      if (!response.ok) throw new Error('Failed to fetch status history');
      return await response.json();
    } catch (error) {
      console.error('Error fetching status history:', error);
      return [];
    }
  }

  async fetchWorkOrders(): Promise<WorkOrder[]> {
    try {
      const response = await fetch('/data/work_orders.json');
      if (!response.ok) throw new Error('Failed to fetch work orders');
      return await response.json();
    } catch (error) {
      console.error('Error fetching work orders:', error);
      return [];
    }
  }

  async getDashboardStats(): Promise<DashboardStats> {
    try {
      const transformers = await this.fetchTransformers();
      const workOrders = await this.fetchWorkOrders();
      
      const statusCounts = transformers.reduce((acc, transformer) => {
        acc[transformer.current_status] = (acc[transformer.current_status] || 0) + 1;
        return acc;
      }, {} as Record<string, number>);

      const averageHealthScore = transformers.reduce((sum, transformer) => 
        sum + transformer.health_score, 0) / transformers.length;

      const activeWorkOrders = workOrders.filter(wo => 
        wo.status === 'open' || wo.status === 'in_progress').length;

      return {
        total_transformers: transformers.length,
        normal: statusCounts.normal || 0,
        maintenance: statusCounts.maintenance || 0,
        emergency: statusCounts.emergency || 0,
        offline: statusCounts.offline || 0,
        average_health_score: Math.round(averageHealthScore),
        active_work_orders: activeWorkOrders
      };
    } catch (error) {
      console.error('Error calculating dashboard stats:', error);
      return {
        total_transformers: 0,
        normal: 0,
        maintenance: 0,
        emergency: 0,
        offline: 0,
        average_health_score: 0,
        active_work_orders: 0
      };
    }
  }

  // Simulate real-time updates
  simulateRealTimeUpdates(callback: (transformer: Transformer) => void) {
    const interval = setInterval(async () => {
      const transformers = await this.fetchTransformers();
      const randomTransformer = transformers[Math.floor(Math.random() * transformers.length)];
      
      // Simulate temperature and load changes
      const updatedTransformer = {
        ...randomTransformer,
        temperature: Math.max(25, Math.min(90, randomTransformer.temperature + (Math.random() - 0.5) * 10)),
        load_percentage: Math.max(0, Math.min(120, randomTransformer.load_percentage + (Math.random() - 0.5) * 20)),
        updated_at: new Date().toISOString()
      };

      callback(updatedTransformer);
    }, 5000); // Update every 5 seconds

    return () => clearInterval(interval);
  }
}

export const apiService = new ApiService();
