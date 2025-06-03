import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useTransformers } from '@/hooks/useTransformers';
import { useAuth } from '@/hooks/useAuth';
import { 
  Zap, 
  Activity, 
  AlertTriangle, 
  Power, 
  TrendingUp,
  MapPin,
  Thermometer,
  Gauge,
  Clock,
  Users
} from 'lucide-react';
import { 
  getStatusColor, 
  getStatusLabel, 
  getHealthScoreColor,
  getTemperatureColor,
  getLoadColor,
  formatDate
} from '@/utils/statusUtils';

export const Dashboard = () => {
  const { user } = useAuth();
  const { 
    transformers, 
    dashboardStats, 
    workOrders,
    isLoading 
  } = useTransformers();

  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
          className="w-16 h-16 border-4 border-cyan-400 border-t-transparent rounded-full"
        />
      </div>
    );
  }

  const statsCards = [
    {
      title: 'Total Transformer',
      value: dashboardStats?.total_transformers || 0,
      icon: Zap,
      color: 'from-cyan-500 to-blue-500',
      shadowColor: 'shadow-cyan-500/25'
    },
    {
      title: 'Normal',
      value: dashboardStats?.normal || 0,
      icon: Activity,
      color: 'from-green-500 to-emerald-500',
      shadowColor: 'shadow-green-500/25'
    },
    {
      title: 'Emergency',
      value: dashboardStats?.emergency || 0,
      icon: AlertTriangle,
      color: 'from-red-500 to-pink-500',
      shadowColor: 'shadow-red-500/25'
    },
    {
      title: 'Offline',
      value: dashboardStats?.offline || 0,
      icon: Power,
      color: 'from-gray-500 to-slate-500',
      shadowColor: 'shadow-gray-500/25'
    }
  ];

  const criticalTransformers = transformers.filter(t => 
    t.current_status === 'emergency' || t.health_score < 60
  );

  const recentWorkOrders = workOrders
    .filter(wo => wo.status === 'open' || wo.status === 'in_progress')
    .slice(0, 5);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 p-4 relative overflow-hidden">
      {/* Background Effects */}
      <div className="absolute inset-0 bg-[url('/images/circuit-pattern.jpg')] opacity-5"></div>
      <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/5 via-purple-500/5 to-blue-500/5"></div>

      <div className="relative z-10 max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <motion.div 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
        >
          <div>
            <h1 className="text-3xl font-bold bg-gradient-to-r from-cyan-400 to-blue-400 bg-clip-text text-transparent">
              Dashboard Monitoring
            </h1>
            <p className="text-slate-400 mt-2">
              Selamat datang, {user?.name} • {currentTime.toLocaleTimeString('id-ID')}
            </p>
          </div>
          
          <div className="flex items-center gap-4">
            <Badge variant="outline" className="border-cyan-400/30 text-cyan-400">
              <Users className="w-4 h-4 mr-2" />
              {user?.role}
            </Badge>
            <Badge variant="outline" className="border-green-400/30 text-green-400">
              <Activity className="w-4 h-4 mr-2" />
              Real-time
            </Badge>
          </div>
        </motion.div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {statsCards.map((stat, index) => (
            <motion.div
              key={stat.title}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
            >
              <Card className={`backdrop-blur-xl bg-black/20 border-slate-600/30 hover:border-slate-500/50 transition-all duration-300 shadow-lg ${stat.shadowColor}`}>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-slate-400 text-sm font-medium">{stat.title}</p>
                      <motion.p 
                        key={stat.value}
                        initial={{ scale: 1.2, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        className="text-3xl font-bold text-white mt-2"
                      >
                        {stat.value}
                      </motion.p>
                    </div>
                    <div className={`w-12 h-12 rounded-xl bg-gradient-to-r ${stat.color} flex items-center justify-center shadow-lg`}>
                      <stat.icon className="w-6 h-6 text-white" />
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Transformer Status Grid */}
          <div className="lg:col-span-2">
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
            >
              <Card className="backdrop-blur-xl bg-black/20 border-slate-600/30">
                <CardHeader>
                  <CardTitle className="text-xl text-white flex items-center gap-2">
                    <MapPin className="w-5 h-5 text-cyan-400" />
                    Status Transformer Real-time
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 max-h-96 overflow-y-auto custom-scrollbar">
                    {transformers.map((transformer, index) => (
                      <motion.div
                        key={transformer.id}
                        initial={{ opacity: 0, scale: 0.9 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ delay: index * 0.05 }}
                        whileHover={{ scale: 1.02 }}
                        className="p-4 rounded-xl bg-slate-800/30 border border-slate-600/20 hover:border-slate-500/40 transition-all duration-300"
                      >
                        <div className="flex justify-between items-start mb-3">
                          <div>
                            <h3 className="font-semibold text-white text-sm">{transformer.name}</h3>
                            <p className="text-xs text-slate-400">{transformer.transformer_code}</p>
                          </div>
                          <Badge className={getStatusColor(transformer.current_status)}>
                            {getStatusLabel(transformer.current_status)}
                          </Badge>
                        </div>
                        
                        <div className="grid grid-cols-3 gap-2 text-xs">
                          <div className="flex items-center gap-1">
                            <TrendingUp className={`w-3 h-3 ${getHealthScoreColor(transformer.health_score)}`} />
                            <span className="text-slate-300">{transformer.health_score}%</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Thermometer className={`w-3 h-3 ${getTemperatureColor(transformer.temperature)}`} />
                            <span className="text-slate-300">{transformer.temperature}°C</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Gauge className={`w-3 h-3 ${getLoadColor(transformer.load_percentage)}`} />
                            <span className="text-slate-300">{transformer.load_percentage}%</span>
                          </div>
                        </div>
                      </motion.div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* Side Panel */}
          <div className="space-y-6">
            {/* Critical Alerts */}
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.4 }}
            >
              <Card className="backdrop-blur-xl bg-black/20 border-red-500/20">
                <CardHeader>
                  <CardTitle className="text-lg text-white flex items-center gap-2">
                    <AlertTriangle className="w-5 h-5 text-red-400" />
                    Alert Kritis
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {criticalTransformers.length === 0 ? (
                      <p className="text-slate-400 text-sm text-center py-4">
                        Tidak ada alert kritis
                      </p>
                    ) : (
                      criticalTransformers.map((transformer) => (
                        <motion.div
                          key={transformer.id}
                          initial={{ opacity: 0, x: 10 }}
                          animate={{ opacity: 1, x: 0 }}
                          className="p-3 rounded-lg bg-red-500/10 border border-red-500/20"
                        >
                          <div className="flex justify-between items-start">
                            <div>
                              <p className="text-red-400 font-medium text-sm">{transformer.name}</p>
                              <p className="text-xs text-slate-400 mt-1">
                                {transformer.current_status === 'emergency' 
                                  ? 'Status Emergency' 
                                  : `Health Score: ${transformer.health_score}%`
                                }
                              </p>
                            </div>
                            <Badge variant="destructive" className="text-xs">
                              {transformer.current_status === 'emergency' ? 'DARURAT' : 'RENDAH'}
                            </Badge>
                          </div>
                        </motion.div>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Recent Work Orders */}
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.5 }}
            >
              <Card className="backdrop-blur-xl bg-black/20 border-slate-600/30">
                <CardHeader>
                  <CardTitle className="text-lg text-white flex items-center gap-2">
                    <Clock className="w-5 h-5 text-blue-400" />
                    Work Orders Aktif
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {recentWorkOrders.length === 0 ? (
                      <p className="text-slate-400 text-sm text-center py-4">
                        Tidak ada work order aktif
                      </p>
                    ) : (
                      recentWorkOrders.map((workOrder) => (
                        <motion.div
                          key={workOrder.id}
                          initial={{ opacity: 0, x: 10 }}
                          animate={{ opacity: 1, x: 0 }}
                          className="p-3 rounded-lg bg-slate-800/30 border border-slate-600/20"
                        >
                          <div className="flex justify-between items-start">
                            <div className="flex-1 min-w-0">
                              <p className="text-white font-medium text-sm truncate">{workOrder.title}</p>
                              <p className="text-xs text-slate-400 mt-1">
                                {formatDate(workOrder.created_at)}
                              </p>
                            </div>
                            <Badge 
                              variant={workOrder.status === 'in_progress' ? 'default' : 'secondary'}
                              className="text-xs ml-2"
                            >
                              {workOrder.status === 'in_progress' ? 'Proses' : 'Buka'}
                            </Badge>
                          </div>
                        </motion.div>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          </div>
        </div>
      </div>

      {/* Custom Scrollbar Styles */}
      <style jsx global>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: rgba(15, 23, 42, 0.3);
          border-radius: 3px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: rgba(34, 197, 254, 0.3);
          border-radius: 3px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: rgba(34, 197, 254, 0.5);
        }
      `}</style>
    </div>
  );
};
