package com.diskee.diskee_project.api.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class SharedFileLinkResponse {
    private UUID linkId;
    private String token;
    private String permission;
    private Instant expiresAt;
    private int downloadCount;
    private Instant createdAt;
    private Long fileId;
    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;
}