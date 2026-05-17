package com.diskee.diskee_project.sdk.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "folders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private DatUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private FolderEntity parentFolder;

    @Column(name = "folder_name", nullable = false, length = 255)
    private String folderName;

    @Column(name = "full_path", nullable = false, columnDefinition = "TEXT")
    private String fullPath;

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