package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.api.exception.UserAlreadyExistsProblem;
import com.diskee.diskee_project.api.request.RegisterRequest;
import com.diskee.diskee_project.api.response.RegisterResponse;
import com.diskee.diskee_project.sdk.service.RegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Аутентификация/Регистрация")
@RestController
@RequestMapping(path = "/app/v4/auth/register", produces = "application/hal+json")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService authService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Регистрация нового пользователя",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Пользователь с таким email уже существует",
                            content = @Content(schema = @Schema(ref = UserAlreadyExistsProblem.SCHEMA_REF))
                    )
            }
    )
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
