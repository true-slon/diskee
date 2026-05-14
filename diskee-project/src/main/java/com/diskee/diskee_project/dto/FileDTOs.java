package com.diskee.diskee_project.dto;

import lombok.Data;
import java.time.Instant;

public class FileDTOs {
    
    @Data
    public static class FileResponse {
        private Long id;
        private String fileName;
        private String fileExtension;
        private String mimeType;
        private Long fileSizeBytes;
        private Long parentFolderId;
        private String storageObjectKey;
        private String previewObjectKey;
        private Boolean isDeleted;
        private Instant createdAt;
        private Instant updatedAt;
    }
    
    @Data
    public static class UploadResponse {
        private String message;
        private FileResponse file;
    }
}