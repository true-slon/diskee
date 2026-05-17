package com.diskee.diskee_project.sdk.data;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shared_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false, length = 50, columnDefinition = "varchar(50) default 'view'")
    @Builder.Default
    private String permission = "view";

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "download_count", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private int downloadCount = 0;

    @Column(name = "created_at", updatable = false, columnDefinition = "timestamptz default now()")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}