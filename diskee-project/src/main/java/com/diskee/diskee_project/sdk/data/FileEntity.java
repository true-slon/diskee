package com.diskee.diskee_project.sdk.data;

import com.diskee.diskee_project.utils.BaseEntity;
import com.diskee.diskee_project.utils.FlagDeletable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.mapping.SoftDeletable;

import java.time.Instant;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity extends BaseEntity implements FlagDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private DatUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private FolderEntity parentFolder;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_extension", length = 50)
    private String fileExtension;

    @Column(name = "mime_type", length = 255)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long fileSizeBytes = 0L;

    @Column(name = "storage_object_key", nullable = false, columnDefinition = "TEXT")
    private String storageObjectKey;

    @Column(name = "preview_object_key", columnDefinition = "TEXT")
    private String previewObjectKey;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", updatable = false, columnDefinition = "timestamptz default now()")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamptz default now()")
    private Instant updatedAt;

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