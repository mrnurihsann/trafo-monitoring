package com.pln.trafo.service;

import com.pln.trafo.entity.Notification;
import com.pln.trafo.entity.User;
import com.pln.trafo.websocket.NotificationWebSocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
public class NotificationService {

    @Inject
    NotificationWebSocket notificationWebSocket;

    @Transactional
    public Notification createNotification(Notification notification) {
        notification.persist();
        
        // Send real-time notification via WebSocket
        if (notification.user != null) {
            notificationWebSocket.sendNotificationToUser(notification.user.id, notification);
        }
        
        return notification;
    }

    @Transactional
    public void createNotificationForUser(User user, String title, String message, 
                                        Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.user = user;
        notification.title = title;
        notification.message = message;
        notification.type = type;
        notification.persist();
        
        // Send real-time notification via WebSocket
        notificationWebSocket.sendNotificationToUser(user.id, notification);
    }

    @Transactional
    public void createNotificationForRole(String title, String message, 
                                        Notification.NotificationType type, User.UserRole role) {
        List<User> users = User.findByRole(role);
        for (User user : users) {
            createNotificationForUser(user, title, message, type);
        }
    }

    @Transactional
    public void createBroadcastNotification(String title, String message, 
                                          Notification.NotificationType type) {
        List<User> allUsers = User.listAll();
        for (User user : allUsers) {
            createNotificationForUser(user, title, message, type);
        }
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = Notification.findById(notificationId);
        if (notification == null) {
            throw new WebApplicationException("Notification tidak ditemukan", Response.Status.NOT_FOUND);
        }
        
        notification.markAsRead();
        return notification;
    }

    @Transactional
    public void markAllAsReadForUser(Long userId) {
        Notification.markAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = Notification.findById(notificationId);
        if (notification == null) {
            throw new WebApplicationException("Notification tidak ditemukan", Response.Status.NOT_FOUND);
        }
        notification.delete();
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return Notification.findByUserId(userId);
    }

    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return Notification.findUnreadByUserId(userId);
    }

    public long getUnreadCountForUser(Long userId) {
        return Notification.countUnreadByUserId(userId);
    }

    public List<Notification> getRecentNotifications(int limit) {
        return Notification.findRecentNotifications(limit);
    }

    public List<Notification> getNotificationsByType(Notification.NotificationType type) {
        return Notification.findByType(type);
    }

    // Utility methods for common notification scenarios
    @Transactional
    public void notifyWorkOrderAssignment(User assignedUser, String workOrderTitle, String transformerCode) {
        String title = "Work Order Baru Ditugaskan";
        String message = String.format("Anda telah ditugaskan work order '%s' untuk transformer %s", 
                                     workOrderTitle, transformerCode);
        createNotificationForUser(assignedUser, title, message, Notification.NotificationType.INFO);
    }

    @Transactional
    public void notifyWorkOrderCompletion(String workOrderTitle, String transformerCode) {
        String title = "Work Order Selesai";
        String message = String.format("Work order '%s' untuk transformer %s telah diselesaikan", 
                                     workOrderTitle, transformerCode);
        createNotificationForRole(title, message, Notification.NotificationType.SUCCESS, User.UserRole.ADMIN);
        createNotificationForRole(title, message, Notification.NotificationType.SUCCESS, User.UserRole.ENGINEER);
    }

    @Transactional
    public void notifyTransformerFault(String transformerName, String transformerCode) {
        String title = "ALERT: Transformer Fault";
        String message = String.format("Transformer %s (%s) mengalami fault dan memerlukan perhatian segera", 
                                     transformerName, transformerCode);
        createNotificationForRole(title, message, Notification.NotificationType.ERROR, User.UserRole.ADMIN);
        createNotificationForRole(title, message, Notification.NotificationType.ERROR, User.UserRole.ENGINEER);
    }

    @Transactional
    public void notifyMaintenanceReminder(String transformerName, String transformerCode, int daysUntilDue) {
        String title = "Maintenance Reminder";
        String message = String.format("Maintenance untuk transformer %s (%s) akan jatuh tempo dalam %d hari", 
                                     transformerName, transformerCode, daysUntilDue);
        createNotificationForRole(title, message, Notification.NotificationType.WARNING, User.UserRole.ENGINEER);
        createNotificationForRole(title, message, Notification.NotificationType.WARNING, User.UserRole.OPERATOR);
    }
}
