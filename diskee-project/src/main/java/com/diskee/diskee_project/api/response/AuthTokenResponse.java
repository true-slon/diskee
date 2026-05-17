package com.diskee.diskee_project.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Schema(description = "Токен доступа")
@Jacksonized
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Getter
@Setter
public class AuthTokenResponse {

    @Schema(description = "Токен")
    private String accessToken;

    @Schema
    private UUID sessionId;

    @Schema(description = "Токен \"запомнить меня\"")
    private String refreshToken;
}