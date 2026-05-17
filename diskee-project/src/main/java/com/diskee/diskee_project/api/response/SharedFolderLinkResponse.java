package com.diskee.diskee_project.api.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class SharedFolderLinkResponse {
    private UUID linkId;
    private String token;
    private String permission;
    private Instant expiresAt;
    private int downloadCount;
    private Instant createdAt;
    private Long folderId;
    private String folderName;
    private String fullPath;
}