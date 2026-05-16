package com.diskee.diskee_project.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Данные аутентификации")
@Data
public final class LoginRequest {

    @NotBlank
    @Schema(description = "Email")
    private String email;

    @NotBlank
    @Schema(description = "Пароль")
    private String password;

    private Boolean rememberMe;

}
