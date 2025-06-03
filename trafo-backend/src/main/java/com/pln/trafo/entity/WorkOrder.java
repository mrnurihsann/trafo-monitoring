package com.pln.trafo.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "work_orders")
public class WorkOrder extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transformer_id", nullable = false)
    @NotNull(message = "Transformer tidak boleh kosong")
    public Transformer transformer;

    @NotBlank(message = "Title tidak boleh kosong")
    @Size(max = 200, message = "Title maksimal 200 karakter")
    @Column(nullable = false)
    public String title;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public WorkOrderStatus status = WorkOrderStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to")
    public User assignedTo;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public enum WorkOrderStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    // Static methods for queries
    public static List<WorkOrder> findByTransformer(Transformer transformer) {
        return find("transformer", transformer).list();
    }

    public static List<WorkOrder> findByTransformerId(Long transformerId) {
        return find("transformer.id = ?1 order by createdAt desc", transformerId).list();
    }

    public static List<WorkOrder> findByAssignedTo(User user) {
        return find("assignedTo", user).list();
    }

    public static List<WorkOrder> findByStatus(WorkOrderStatus status) {
        return find("status", status).list();
    }

    public static List<WorkOrder> findPendingWorkOrders() {
        return find("status", WorkOrderStatus.PENDING).list();
    }

    public static List<WorkOrder> findActiveWorkOrders() {
        return find("status in (?1, ?2)", WorkOrderStatus.PENDING, WorkOrderStatus.IN_PROGRESS).list();
    }

    public static long countByStatus(WorkOrderStatus status) {
        return count("status", status);
    }

    public static List<WorkOrder> findByAssignedToAndStatus(User user, WorkOrderStatus status) {
        return find("assignedTo = ?1 and status = ?2", user, status).list();
    }

    public static List<WorkOrder> findOverdueWorkOrders() {
        return find("status in (?1, ?2) and createdAt < ?3", 
                    WorkOrderStatus.PENDING, WorkOrderStatus.IN_PROGRESS, 
                    LocalDateTime.now().minusDays(7)).list();
    }

    // Helper method to complete work order
    public void complete() {
        this.status = WorkOrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
