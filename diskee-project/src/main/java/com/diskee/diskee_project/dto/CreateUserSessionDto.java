package com.diskee.diskee_project.dto;


import com.diskee.diskee_project.sdk.data.DatUserEntity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CreateUserSessionDto {
    private DatUserEntity user;
    private String rawRefreshToken;
    private String ipAddress;
    private String userAgent;
    private Instant expiresAt;
}