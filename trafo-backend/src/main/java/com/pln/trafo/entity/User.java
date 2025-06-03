package com.pln.trafo.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
@UserDefinition
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Username
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus antara 3-50 karakter")
    @Column(unique = true, nullable = false)
    public String username;

    @Password
    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 6, message = "Password minimal 6 karakter")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String password;

    @Email(message = "Format email tidak valid")
    @NotBlank(message = "Email tidak boleh kosong")
    @Column(unique = true, nullable = false)
    public String email;

    @Roles
    @NotBlank(message = "Role tidak boleh kosong")
    @Column(nullable = false)
    public String role; // <- Diubah dari enum ke String

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Relasi ke tabel transformer (sebagai engineer)
    @OneToMany(mappedBy = "picEngineer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<Transformer> assignedTransformers;

    // Relasi ke tabel work orders
    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<WorkOrder> assignedWorkOrders;

    // Relasi ke tabel notifikasi
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<Notification> notifications;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === STATIC METHODS ===

    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static List<User> findByRole(String role) {
        return find("role", role).list();
    }

    public static List<User> findEngineers() {
        return find("role", "ENGINEER").list();
    }

    // Opsional: cek role dengan helper
    public boolean hasRole(String targetRole) {
        return role != null && role.equalsIgnoreCase(targetRole);
    }
}
