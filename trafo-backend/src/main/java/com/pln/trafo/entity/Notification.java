package com.pln.trafo.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notifications")
public class Notification extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public User user;

    @NotBlank(message = "Title tidak boleh kosong")
    @Size(max = 200, message = "Title maksimal 200 karakter")
    @Column(nullable = false)
    public String title;

    @NotBlank(message = "Message tidak boleh kosong")
    @Column(columnDefinition = "TEXT", nullable = false)
    public String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public NotificationType type = NotificationType.INFO;

    @Column(name = "read_status", nullable = false)
    public Boolean readStatus = false;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        INFO, WARNING, ERROR, SUCCESS
    }

    // Static methods for queries
    public static List<Notification> findByUser(User user) {
        return find("user = ?1 order by createdAt desc", user).list();
    }

    public static List<Notification> findByUserId(Long userId) {
        return find("user.id = ?1 order by createdAt desc", userId).list();
    }

    public static List<Notification> findUnreadByUser(User user) {
        return find("user = ?1 and readStatus = false order by createdAt desc", user).list();
    }

    public static List<Notification> findUnreadByUserId(Long userId) {
        return find("user.id = ?1 and readStatus = false order by createdAt desc", userId).list();
    }

    public static long countUnreadByUser(User user) {
        return count("user = ?1 and readStatus = false", user);
    }

    public static long countUnreadByUserId(Long userId) {
        return count("user.id = ?1 and readStatus = false", userId);
    }

    public static List<Notification> findByType(NotificationType type) {
        return find("type = ?1 order by createdAt desc", type).list();
    }

    public static List<Notification> findRecentNotifications(int limit) {
        return find("order by createdAt desc").page(0, limit).list();
    }

    public static void markAsReadByUser(User user) {
        update("readStatus = true where user = ?1 and readStatus = false", user);
    }

    public static void markAsReadByUserId(Long userId) {
        update("readStatus = true where user.id = ?1 and readStatus = false", userId);
    }

    // Helper method to mark this notification as read
    public void markAsRead() {
        this.readStatus = true;
    }

    // Static method to create notification for all users with specific role
    public static void createForRole(String title, String message, NotificationType type, User.UserRole role) {
        List<User> users = User.findByRole(role);
        for (User user : users) {
            Notification notification = new Notification();
            notification.user = user;
            notification.title = title;
            notification.message = message;
            notification.type = type;
            notification.persist();
        }
    }

    // Static method to create notification for specific user
    public static void createForUser(User user, String title, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.user = user;
        notification.title = title;
        notification.message = message;
        notification.type = type;
        notification.persist();
    }
}
