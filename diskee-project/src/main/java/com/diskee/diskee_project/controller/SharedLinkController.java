package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.api.request.CreateSharedLinkRequest;
import com.diskee.diskee_project.api.response.SharedLinkInfoResponse;
import com.diskee.diskee_project.api.response.SharedLinkResponse;

import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.SharedLinkEntity;
import com.diskee.diskee_project.sdk.service.FileService;
import com.diskee.diskee_project.sdk.service.SharedLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Sharing", description = "Публичные ссылки")
@RestController
@RequestMapping(path = "/app/v4/share", produces = "application/hal+json")
@RequiredArgsConstructor
public class SharedLinkController {

    private final SharedLinkService sharedLinkService;
    private final FileService fileService;

    @PostMapping("/file/{fileId}")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Создать ссылку на файл",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Ссылка создана"),
                    @ApiResponse(responseCode = "404", description = "Файл не найден")
            }
    )
    public SharedLinkResponse createForFile(
            @PathVariable Long fileId,
            @RequestBody CreateSharedLinkRequest request
    ) {
        return sharedLinkService.createForFile(fileId, request);
    }

    @PostMapping("/folder/{folderId}")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Создать ссылку на папку",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Ссылка создана"),
                    @ApiResponse(responseCode = "404", description = "Папка не найдена")
            }
    )
    public SharedLinkResponse createForFolder(
            @PathVariable Long folderId,
            @RequestBody CreateSharedLinkRequest request
    ) {
        return sharedLinkService.createForFolder(folderId, request);
    }

    @GetMapping("/{token}")
    @Operation(
            summary = "Получить информацию по токену (публичный)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация получена"),
                    @ApiResponse(responseCode = "404", description = "Ссылка не найдена или истекла")
            }
    )
    public SharedLinkInfoResponse resolve(@PathVariable String token) {
        return sharedLinkService.resolveToken(token);
    }

    @DeleteMapping("/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Удалить ссылку",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Ссылка удалена")
            }
    )
    public void delete(@PathVariable UUID linkId) {
        sharedLinkService.delete(linkId);
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Получить все мои публичные ссылки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список ссылок"),
                    @ApiResponse(responseCode = "401", description = "Не авторизован")
            }
    )
    public List<SharedLinkResponse> getMySharedLinks() {
        return sharedLinkService.getUserSharedLinks();
    }
}