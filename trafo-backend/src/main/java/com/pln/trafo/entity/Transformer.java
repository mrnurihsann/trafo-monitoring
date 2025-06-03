package com.pln.trafo.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transformers")
public class Transformer extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "Nama trafo tidak boleh kosong")
    @Size(max = 100, message = "Nama trafo maksimal 100 karakter")
    @Column(name = "nama_trafo", nullable = false)
    public String namaTrafo;

    @NotBlank(message = "Kode trafo tidak boleh kosong")
    @Size(max = 50, message = "Kode trafo maksimal 50 karakter")
    @Column(name = "kode_trafo", unique = true, nullable = false)
    public String kodeTrafo;

    @NotBlank(message = "Lokasi tidak boleh kosong")
    @Size(max = 200, message = "Lokasi maksimal 200 karakter")
    @Column(nullable = false)
    public String lokasi;

    @NotNull(message = "Kapasitas KVA tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Kapasitas KVA harus lebih besar dari 0")
    @Column(name = "kapasitas_kva", precision = 10, scale = 2, nullable = false)
    public BigDecimal kapasitasKva;

    @NotNull(message = "Tegangan KV tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tegangan KV harus lebih besar dari 0")
    @Column(name = "tegangan_kv", precision = 8, scale = 2, nullable = false)
    public BigDecimal teganganKv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TransformerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Priority priority = Priority.MEDIUM;

    @Min(value = 0, message = "Progress minimal 0")
    @Max(value = 100, message = "Progress maksimal 100")
    @Column(nullable = false)
    public Integer progress = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pic_engineer")
    public User picEngineer;

    @Column(name = "tanggal_mulai")
    public LocalDate tanggalMulai;

    @Column(name = "tanggal_target")
    public LocalDate tanggalTarget;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "transformer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<StatusHistory> statusHistories;

    @OneToMany(mappedBy = "transformer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<WorkOrder> workOrders;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransformerStatus {
        OPERATIONAL, MAINTENANCE, FAULT, OFFLINE
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Static methods for queries
    public static List<Transformer> findByStatus(TransformerStatus status) {
        return find("status", status).list();
    }

    public static List<Transformer> findByPicEngineer(User engineer) {
        return find("picEngineer", engineer).list();
    }

    public static List<Transformer> findByPriority(Priority priority) {
        return find("priority", priority).list();
    }

    public static Transformer findByKodeTrafo(String kodeTrafo) {
        return find("kodeTrafo", kodeTrafo).firstResult();
    }

    public static List<Transformer> findByLokasi(String lokasi) {
        return find("lokasi like ?1", "%" + lokasi + "%").list();
    }

    public static long countByStatus(TransformerStatus status) {
        return count("status", status);
    }

    public static List<Transformer> findCriticalTransformers() {
        return find("priority = ?1 OR status = ?2", Priority.CRITICAL, TransformerStatus.FAULT).list();
    }
}
