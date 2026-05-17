package com.diskee.diskee_project.dto;

import lombok.Data;

public class StorageDTOs {
    
    @Data
    public static class StorageInfoResponse {
        private Long storageUsedBytes;
        private Long storageLimitBytes;
        private Double usagePercentage;
        private String storageUsedFormatted;
        private String storageLimitFormatted;
    }
}