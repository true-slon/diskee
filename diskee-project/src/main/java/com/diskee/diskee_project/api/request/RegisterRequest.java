package com.diskee.diskee_project.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email обязателен")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 255, message = "Пароль должен содержать от 8 символов")
    private String password;

    @Size(max = 255)
    private String displayName;
}