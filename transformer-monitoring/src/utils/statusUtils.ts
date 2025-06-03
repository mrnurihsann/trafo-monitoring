import { TransformerStatus } from '@/types';

export const getStatusColor = (status: TransformerStatus): string => {
  const statusColors = {
    normal: 'text-green-400 bg-green-400/10 border-green-400/20',
    pending: 'text-yellow-400 bg-yellow-400/10 border-yellow-400/20',
    in_progress: 'text-blue-400 bg-blue-400/10 border-blue-400/20',
    testing: 'text-purple-400 bg-purple-400/10 border-purple-400/20',
    completed: 'text-green-500 bg-green-500/10 border-green-500/20',
    maintenance: 'text-orange-400 bg-orange-400/10 border-orange-400/20',
    emergency: 'text-red-400 bg-red-400/10 border-red-400/20',
    offline: 'text-gray-400 bg-gray-400/10 border-gray-400/20'
  };
  
  return statusColors[status] || statusColors.pending;
};

export const getStatusBadgeVariant = (status: TransformerStatus): 'default' | 'secondary' | 'destructive' => {
  switch (status) {
    case 'emergency':
    case 'offline':
      return 'destructive';
    case 'normal':
    case 'completed':
      return 'default';
    default:
      return 'secondary';
  }
};

export const getStatusIcon = (status: TransformerStatus): string => {
  const statusIcons = {
    normal: '🟢',
    pending: '🟡',
    in_progress: '🔵',
    testing: '🟣',
    completed: '✅',
    maintenance: '🟠',
    emergency: '🔴',
    offline: '⚫'
  };
  
  return statusIcons[status] || '⚪';
};

export const getStatusLabel = (status: TransformerStatus): string => {
  const statusLabels = {
    normal: 'Normal',
    pending: 'Menunggu',
    in_progress: 'Dalam Proses',
    testing: 'Testing',
    completed: 'Selesai',
    maintenance: 'Maintenance',
    emergency: 'Darurat',
    offline: 'Offline'
  };
  
  return statusLabels[status] || status;
};

export const getPriorityColor = (priority: number): string => {
  if (priority >= 5) return 'text-red-400 bg-red-400/10';
  if (priority >= 4) return 'text-orange-400 bg-orange-400/10';
  if (priority >= 3) return 'text-yellow-400 bg-yellow-400/10';
  if (priority >= 2) return 'text-blue-400 bg-blue-400/10';
  return 'text-green-400 bg-green-400/10';
};

export const getPriorityLabel = (priority: number): string => {
  if (priority >= 5) return 'Sangat Tinggi';
  if (priority >= 4) return 'Tinggi';
  if (priority >= 3) return 'Sedang';
  if (priority >= 2) return 'Rendah';
  return 'Sangat Rendah';
};

export const getHealthScoreColor = (score: number): string => {
  if (score >= 90) return 'text-green-400';
  if (score >= 70) return 'text-yellow-400';
  if (score >= 50) return 'text-orange-400';
  return 'text-red-400';
};

export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const formatDateShort = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('id-ID', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

export const getTemperatureColor = (temperature: number): string => {
  if (temperature >= 80) return 'text-red-400';
  if (temperature >= 65) return 'text-orange-400';
  if (temperature >= 50) return 'text-yellow-400';
  return 'text-green-400';
};

export const getLoadColor = (loadPercentage: number): string => {
  if (loadPercentage >= 100) return 'text-red-400';
  if (loadPercentage >= 85) return 'text-orange-400';
  if (loadPercentage >= 70) return 'text-yellow-400';
  return 'text-green-400';
};
