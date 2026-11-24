package com.facundo.trazabilidad.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected AppUser() {
    }

    public AppUser(String email, String passwordHash, UserRole role) {
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}
