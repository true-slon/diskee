package com.diskee.diskee_project.sdk.data;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "dat_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "storage_used_bytes", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long storageUsedBytes = 0L;

    @Column(name = "storage_limit_bytes", nullable = false, columnDefinition = "bigint default 10737418240") // 10 GB
    @Builder.Default
    private Long storageLimitBytes = 10_737_418_240L;

    @Column(name = "created_at", updatable = false, columnDefinition = "timestamptz default now()")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamptz default now()")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}