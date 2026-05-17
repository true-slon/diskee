package com.diskee.diskee_project.api.response;

import lombok.Data;

import java.time.Instant;

@Data
public class SharedLinkInfoResponse {
    private String token;
    private String type; // "file" или "folder"
    private Long targetId;
    private String name;
    private String permission;
    private Instant expiresAt;
    private int downloadCount;
}