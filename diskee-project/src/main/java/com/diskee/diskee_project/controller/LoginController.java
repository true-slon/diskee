package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.api.request.LoginRequest;
import com.diskee.diskee_project.api.response.AuthTokenResponse;
import com.diskee.diskee_project.sdk.service.JWTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Аутентификация/Регистрация")
@RestController
@RequestMapping(path = "/app/v4/auth/login", produces = "application/hal+json")
@RequiredArgsConstructor
public class LoginController {

    private final JWTService jwtService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Вход в систему",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход"),
                    @ApiResponse(responseCode = "401", description = "Неверный email или пароль")
            }
    )
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return jwtService.login(request, ipAddress, userAgent);
    }
}