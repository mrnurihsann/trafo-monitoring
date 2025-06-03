import { useState, useEffect } from 'react';
import { Transformer, StatusHistory, WorkOrder, DashboardStats } from '@/types';
import { apiService } from '@/services/api';

export const useTransformers = () => {
  const [transformers, setTransformers] = useState<Transformer[]>([]);
  const [statusHistory, setStatusHistory] = useState<StatusHistory[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [dashboardStats, setDashboardStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Load initial data
  useEffect(() => {
    loadData();
  }, []);

  // Set up real-time updates
  useEffect(() => {
    const cleanup = apiService.simulateRealTimeUpdates((updatedTransformer) => {
      setTransformers(prev => 
        prev.map(t => t.id === updatedTransformer.id ? updatedTransformer : t)
      );
      
      // Recalculate dashboard stats when transformer updates
      updateDashboardStats();
    });

    return cleanup;
  }, []);

  const loadData = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const [transformersData, statusHistoryData, workOrdersData, statsData] = await Promise.all([
        apiService.fetchTransformers(),
        apiService.fetchStatusHistory(),
        apiService.fetchWorkOrders(),
        apiService.getDashboardStats()
      ]);

      setTransformers(transformersData);
      setStatusHistory(statusHistoryData);
      setWorkOrders(workOrdersData);
      setDashboardStats(statsData);
    } catch (err) {
      setError('Failed to load data');
      console.error('Data loading error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const updateDashboardStats = async () => {
    try {
      const stats = await apiService.getDashboardStats();
      setDashboardStats(stats);
    } catch (err) {
      console.error('Error updating dashboard stats:', err);
    }
  };

  const updateTransformerStatus = (transformerId: number, newStatus: Transformer['current_status'], reason: string) => {
    setTransformers(prev => 
      prev.map(transformer => {
        if (transformer.id === transformerId) {
          const updated = {
            ...transformer,
            current_status: newStatus,
            updated_at: new Date().toISOString()
          };

          // Add to status history
          const historyEntry: StatusHistory = {
            id: Date.now(),
            transformer_id: transformerId,
            previous_status: transformer.current_status,
            new_status: newStatus,
            changed_by: 1, // In real app, would be current user
            change_reason: reason,
            changed_at: new Date().toISOString()
          };
          setStatusHistory(prev => [historyEntry, ...prev]);

          return updated;
        }
        return transformer;
      })
    );

    updateDashboardStats();
  };

  const getTransformersByStatus = (status: Transformer['current_status']) => {
    return transformers.filter(t => t.current_status === status);
  };

  const getTransformerById = (id: number) => {
    return transformers.find(t => t.id === id);
  };

  const getWorkOrdersByTransformerId = (transformerId: number) => {
    return workOrders.filter(wo => wo.transformer_id === transformerId);
  };

  const getStatusHistoryByTransformerId = (transformerId: number) => {
    return statusHistory.filter(sh => sh.transformer_id === transformerId);
  };

  return {
    transformers,
    statusHistory,
    workOrders,
    dashboardStats,
    isLoading,
    error,
    updateTransformerStatus,
    getTransformersByStatus,
    getTransformerById,
    getWorkOrdersByTransformerId,
    getStatusHistoryByTransformerId,
    refreshData: loadData
  };
};
