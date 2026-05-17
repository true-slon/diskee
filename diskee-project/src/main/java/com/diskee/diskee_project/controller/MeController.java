package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Auth", description = "Аутентификация/Регистрация")
@RestController
@RequestMapping(path = "/app/v4/auth", produces = "application/hal+json")
@RequiredArgsConstructor
public class MeController {

    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Получить данные текущего пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные пользователя"),
                    @ApiResponse(responseCode = "401", description = "Не авторизован")
            }
    )
    public ResponseEntity<Map<String, Object>> me() {
        DatUserEntity user = currentUserService.getUser();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("email", user.getEmail());
        result.put("displayName", user.getDisplayName());
        result.put("storageUsedBytes", user.getStorageUsedBytes());
        result.put("storageLimitBytes", user.getStorageLimitBytes());
        result.put("createdAt", user.getCreatedAt());
        return ResponseEntity.ok(result);
    }
}