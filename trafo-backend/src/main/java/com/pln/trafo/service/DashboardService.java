package com.pln.trafo.service;

import com.pln.trafo.dto.DashboardStats;
import com.pln.trafo.entity.Transformer;
import com.pln.trafo.entity.WorkOrder;
import com.pln.trafo.entity.Notification;
import com.pln.trafo.entity.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class DashboardService {

    @Inject
    EntityManager em;

    public DashboardStats getDashboardStats(Long userId) {
        // Transformer statistics
        long totalTransformers = Transformer.count();
        long operationalTransformers = Transformer.countByStatus(Transformer.TransformerStatus.OPERATIONAL);
        long maintenanceTransformers = Transformer.countByStatus(Transformer.TransformerStatus.MAINTENANCE);
        long faultTransformers = Transformer.countByStatus(Transformer.TransformerStatus.FAULT);
        long offlineTransformers = Transformer.countByStatus(Transformer.TransformerStatus.OFFLINE);

        // Work Order statistics
        long totalWorkOrders = WorkOrder.count();
        long pendingWorkOrders = WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.PENDING);
        long inProgressWorkOrders = WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.IN_PROGRESS);
        long completedWorkOrders = WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.COMPLETED);

        // Priority statistics
        long criticalTransformers = Transformer.count("priority = ?1", Transformer.Priority.CRITICAL);
        long highPriorityTransformers = Transformer.count("priority = ?1", Transformer.Priority.HIGH);

        // Notification statistics for current user
        long unreadNotifications = userId != null ? 
            Notification.countUnreadByUserId(userId) : 0;

        DashboardStats stats = new DashboardStats(
            totalTransformers,
            operationalTransformers,
            maintenanceTransformers,
            faultTransformers,
            offlineTransformers,
            totalWorkOrders,
            pendingWorkOrders,
            inProgressWorkOrders,
            completedWorkOrders,
            criticalTransformers,
            highPriorityTransformers,
            unreadNotifications
        );

        // Status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("OPERATIONAL", operationalTransformers);
        statusDistribution.put("MAINTENANCE", maintenanceTransformers);
        statusDistribution.put("FAULT", faultTransformers);
        statusDistribution.put("OFFLINE", offlineTransformers);
        stats.statusDistribution = statusDistribution;

        // Priority distribution
        Map<String, Long> priorityDistribution = new HashMap<>();
        priorityDistribution.put("LOW", Transformer.count("priority = ?1", Transformer.Priority.LOW));
        priorityDistribution.put("MEDIUM", Transformer.count("priority = ?1", Transformer.Priority.MEDIUM));
        priorityDistribution.put("HIGH", highPriorityTransformers);
        priorityDistribution.put("CRITICAL", criticalTransformers);
        stats.priorityDistribution = priorityDistribution;

        // Work Order status distribution
        Map<String, Long> workOrderStatusDistribution = new HashMap<>();
        workOrderStatusDistribution.put("PENDING", pendingWorkOrders);
        workOrderStatusDistribution.put("IN_PROGRESS", inProgressWorkOrders);
        workOrderStatusDistribution.put("COMPLETED", completedWorkOrders);
        workOrderStatusDistribution.put("CANCELLED", WorkOrder.countByStatus(WorkOrder.WorkOrderStatus.CANCELLED));
        stats.workOrderStatusDistribution = workOrderStatusDistribution;

        return stats;
    }

    public Map<String, Object> getRecentActivities() {
        Map<String, Object> activities = new HashMap<>();

        // Recent status changes (last 10)
        String statusHistoryQuery = """
            SELECT h FROM StatusHistory h 
            JOIN FETCH h.transformer t 
            JOIN FETCH h.changedBy u 
            ORDER BY h.changedAt DESC
            """;
        var recentStatusChanges = em.createQuery(statusHistoryQuery)
            .setMaxResults(10)
            .getResultList();
        activities.put("recentStatusChanges", recentStatusChanges);

        // Recent work orders (last 10)
        String workOrderQuery = """
            SELECT w FROM WorkOrder w 
            JOIN FETCH w.transformer t 
            LEFT JOIN FETCH w.assignedTo u 
            ORDER BY w.createdAt DESC
            """;
        var recentWorkOrders = em.createQuery(workOrderQuery)
            .setMaxResults(10)
            .getResultList();
        activities.put("recentWorkOrders", recentWorkOrders);

        // Critical alerts (transformers with fault status or critical priority)
        var criticalAlerts = Transformer.find(
            "status = ?1 OR priority = ?2 ORDER BY updatedAt DESC", 
            Transformer.TransformerStatus.FAULT, 
            Transformer.Priority.CRITICAL
        ).page(0, 5).list();
        activities.put("criticalAlerts", criticalAlerts);

        return activities;
    }

    public Map<String, Object> getPersonalDashboard(Long userId) {
        User currentUser = User.findById(userId);
        if (currentUser == null) {
            return new HashMap<>();
        }

        Map<String, Object> personalDashboard = new HashMap<>();

        // User's assigned transformers (for engineers)
        if (currentUser.role == User.UserRole.ENGINEER) {
            var assignedTransformers = Transformer.findByPicEngineer(currentUser);
            personalDashboard.put("assignedTransformers", assignedTransformers);
            personalDashboard.put("assignedTransformersCount", assignedTransformers.size());
        }

        // User's assigned work orders
        var assignedWorkOrders = WorkOrder.findByAssignedTo(currentUser);
        personalDashboard.put("assignedWorkOrders", assignedWorkOrders);
        personalDashboard.put("assignedWorkOrdersCount", assignedWorkOrders.size());

        // User's pending work orders
        var pendingWorkOrders = WorkOrder.findByAssignedToAndStatus(currentUser, WorkOrder.WorkOrderStatus.PENDING);
        personalDashboard.put("pendingWorkOrders", pendingWorkOrders);
        personalDashboard.put("pendingWorkOrdersCount", pendingWorkOrders.size());

        // User's notifications
        var unreadNotifications = Notification.findUnreadByUserId(userId);
        personalDashboard.put("unreadNotifications", unreadNotifications);
        personalDashboard.put("unreadNotificationsCount", unreadNotifications.size());

        return personalDashboard;
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        // Calculate system health metrics
        long totalTransformers = Transformer.count();
        long operationalTransformers = Transformer.countByStatus(Transformer.TransformerStatus.OPERATIONAL);
        long faultTransformers = Transformer.countByStatus(Transformer.TransformerStatus.FAULT);
        
        double operationalPercentage = totalTransformers > 0 ? 
            (double) operationalTransformers / totalTransformers * 100 : 0;
        double faultPercentage = totalTransformers > 0 ? 
            (double) faultTransformers / totalTransformers * 100 : 0;

        health.put("systemHealthScore", operationalPercentage);
        health.put("operationalPercentage", operationalPercentage);
        health.put("faultPercentage", faultPercentage);
        
        // System status based on fault percentage
        String systemStatus;
        if (faultPercentage == 0) {
            systemStatus = "EXCELLENT";
        } else if (faultPercentage < 5) {
            systemStatus = "GOOD";
        } else if (faultPercentage < 15) {
            systemStatus = "WARNING";
        } else {
            systemStatus = "CRITICAL";
        }
        
        health.put("systemStatus", systemStatus);
        health.put("totalTransformers", totalTransformers);
        health.put("operationalTransformers", operationalTransformers);
        health.put("faultTransformers", faultTransformers);

        return health;
    }
}
