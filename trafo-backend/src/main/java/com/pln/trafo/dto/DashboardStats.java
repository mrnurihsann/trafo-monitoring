package com.pln.trafo.dto;

import java.util.Map;

public class DashboardStats {
    
    public long totalTransformers;
    public long operationalTransformers;
    public long maintenanceTransformers;
    public long faultTransformers;
    public long offlineTransformers;
    
    public long totalWorkOrders;
    public long pendingWorkOrders;
    public long inProgressWorkOrders;
    public long completedWorkOrders;
    
    public long criticalTransformers;
    public long highPriorityTransformers;
    
    public long unreadNotifications;
    
    public Map<String, Long> statusDistribution;
    public Map<String, Long> priorityDistribution;
    public Map<String, Long> workOrderStatusDistribution;

    public DashboardStats() {}

    public DashboardStats(
            long totalTransformers,
            long operationalTransformers,
            long maintenanceTransformers,
            long faultTransformers,
            long offlineTransformers,
            long totalWorkOrders,
            long pendingWorkOrders,
            long inProgressWorkOrders,
            long completedWorkOrders,
            long criticalTransformers,
            long highPriorityTransformers,
            long unreadNotifications) {
        
        this.totalTransformers = totalTransformers;
        this.operationalTransformers = operationalTransformers;
        this.maintenanceTransformers = maintenanceTransformers;
        this.faultTransformers = faultTransformers;
        this.offlineTransformers = offlineTransformers;
        
        this.totalWorkOrders = totalWorkOrders;
        this.pendingWorkOrders = pendingWorkOrders;
        this.inProgressWorkOrders = inProgressWorkOrders;
        this.completedWorkOrders = completedWorkOrders;
        
        this.criticalTransformers = criticalTransformers;
        this.highPriorityTransformers = highPriorityTransformers;
        
        this.unreadNotifications = unreadNotifications;
    }
}
