package com.pln.trafo.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "status_history")
public class StatusHistory extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transformer_id", nullable = false)
    @NotNull(message = "Transformer tidak boleh kosong")
    public Transformer transformer;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    public Transformer.TransformerStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    @NotNull(message = "Status baru tidak boleh kosong")
    public Transformer.TransformerStatus newStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changed_by", nullable = false)
    @NotNull(message = "Changed by tidak boleh kosong")
    public User changedBy;

    @Column(name = "changed_at")
    public LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }

    // Static methods for queries
    public static List<StatusHistory> findByTransformer(Transformer transformer) {
        return find("transformer", transformer).list();
    }

    public static List<StatusHistory> findByTransformerId(Long transformerId) {
        return find("transformer.id = ?1 order by changedAt desc", transformerId).list();
    }

    public static List<StatusHistory> findByChangedBy(User user) {
        return find("changedBy", user).list();
    }

    public static List<StatusHistory> findByNewStatus(Transformer.TransformerStatus status) {
        return find("newStatus", status).list();
    }

    public static List<StatusHistory> findRecentChanges(int limit) {
        return find("order by changedAt desc").page(0, limit).list();
    }

    public static List<StatusHistory> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return find("changedAt between ?1 and ?2 order by changedAt desc", startDate, endDate).list();
    }
}
