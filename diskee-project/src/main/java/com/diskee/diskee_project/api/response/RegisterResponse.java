package com.diskee.diskee_project.api.response;

import lombok.Data;

import lombok.Builder;

import java.time.Instant;

@Data
@Builder
public class RegisterResponse {

    private Long id;
    private String email;
    private String displayName;
    private Long storageLimitBytes;
    private Instant createdAt;
}