package com.diskee.diskee_project.sdk.data;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "trash_bin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrashBinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private DatUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;

    @Column(name = "original_path", nullable = false, columnDefinition = "TEXT")
    private String originalPath;

    @Column(name = "deleted_at", columnDefinition = "timestamptz default now()")
    private Instant deletedAt;

    @Column(name = "auto_delete_at", nullable = false)
    private Instant autoDeleteAt;

    @PrePersist
    protected void onCreate() {
        if (deletedAt == null) deletedAt = Instant.now();
    }
}