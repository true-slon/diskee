package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.sdk.service.JWTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Аутентификация/Регистрация")
@RestController
@RequestMapping(path = "/app/v4/auth", produces = "application/hal+json")
@RequiredArgsConstructor
public class LogoutController {

    private final JWTService jwtService;

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Выход из системы (текущая сессия)",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Сессия завершена"),
                    @ApiResponse(responseCode = "401", description = "Не авторизован")
            }
    )
    public void logout(@RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
        if (refreshToken != null) {
            jwtService.logout(refreshToken);
        }
    }

    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Выход со всех устройств",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Все сессии завершены"),
                    @ApiResponse(responseCode = "401", description = "Не авторизован")
            }
    )
    public void logoutAll(@RequestHeader("X-User-Id") Long userId) {
        jwtService.logoutAll(userId);
    }
}