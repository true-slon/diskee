package com.diskee.diskee_project.sdk.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "link_files_folders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkFilesFoldersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private FolderEntity folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private DatUserEntity ownerUser;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "is_original", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean isOriginal = false;

    @Column(name = "created_at", updatable = false, columnDefinition = "timestamptz default now()")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}