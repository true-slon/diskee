package com.diskee.diskee_project.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public class FolderDTOs {
    
    @Data
    public static class FolderRequest {
        @NotBlank
        private String folderName;
        private Long parentFolderId;
    }
    
    @Data
    public static class FolderResponse {
        private Long id;
        private String folderName;
        private String fullPath;
        private Long parentFolderId;
        private Instant createdAt;
        private Instant updatedAt;
    }
    
    @Data
    public static class FolderContentsResponse {
        private List<FolderResponse> folders;
        private List<FileDTOs.FileResponse> files;
        private Integer totalFolders;
        private Integer totalFiles;
    }
}