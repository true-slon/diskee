package com.diskee.diskee_project.api.request;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateSharedLinkRequest {
    private String permission; // "view" или "download"
    private Instant expiresAt; // null = бессрочно
}
