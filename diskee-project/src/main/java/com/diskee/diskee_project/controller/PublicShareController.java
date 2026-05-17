package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.api.response.SharedFolderContentsResponse;
import com.diskee.diskee_project.api.response.SharedLinkInfoResponse;
import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.FolderEntity;
import com.diskee.diskee_project.sdk.data.SharedLinkEntity;
import com.diskee.diskee_project.sdk.service.FileService;
import com.diskee.diskee_project.sdk.service.FolderService;
import com.diskee.diskee_project.sdk.service.SharedLinkService;
import com.diskee.diskee_project.dto.FileDTOs;
import com.diskee.diskee_project.dto.FolderDTOs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Public Share", description = "Публичный доступ по ссылке")
@RestController
@RequestMapping(path = "/app/v4/public/share", produces = "application/hal+json")
@RequiredArgsConstructor
public class PublicShareController {

    private final SharedLinkService sharedLinkService;
    private final FileService fileService;
    private final FolderService folderService;

    @GetMapping("/{token}")
    @Operation(
            summary = "Информация по токену",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация получена"),
                    @ApiResponse(responseCode = "404", description = "Ссылка не найдена или истекла")
            }
    )
    public SharedLinkInfoResponse resolve(@PathVariable String token) {
        return sharedLinkService.resolveToken(token);
    }

    @GetMapping("/{token}/file/download")
    @Operation(
            summary = "Скачать файл по публичной ссылке",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл"),
                    @ApiResponse(responseCode = "403", description = "Ссылка не разрешает скачивание"),
                    @ApiResponse(responseCode = "404", description = "Ссылка не найдена или истекла")
            }
    )
    public ResponseEntity<Resource> downloadFile(@PathVariable String token) {
        SharedLinkEntity link = sharedLinkService.validateAndGet(token);

        if (link.getFile() == null) {
            throw new RuntimeException("This link points to a folder, not a file");
        }
        if ("view".equals(link.getPermission())) {
            throw new RuntimeException("Download not allowed for this link");
        }

        FileEntity file = link.getFile();
        Resource resource = fileService.getFileByKey(file.getStorageObjectKey());
        sharedLinkService.incrementDownloadCount(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .body(resource);
    }

    @GetMapping("/{token}/file/view")
    @Operation(
            summary = "Просмотр файла по публичной ссылке (inline)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Файл"),
                    @ApiResponse(responseCode = "404", description = "Ссылка не найдена или истекла")
            }
    )
    public ResponseEntity<Resource> viewFile(@PathVariable String token) {
        SharedLinkEntity link = sharedLinkService.validateAndGet(token);

        if (link.getFile() == null) {
            throw new RuntimeException("This link points to a folder, not a file");
        }

        FileEntity file = link.getFile();
        Resource resource = fileService.getFileByKey(file.getStorageObjectKey());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .body(resource);
    }

    @GetMapping("/{token}/folder")
    @Operation(
            summary = "Получить содержимое папки по публичной ссылке",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Содержимое папки"),
                    @ApiResponse(responseCode = "404", description = "Ссылка не найдена или истекла")
            }
    )
    public SharedFolderContentsResponse getFolderContents(@PathVariable String token) {
        SharedLinkEntity link = sharedLinkService.validateAndGet(token);

        if (link.getFolder() == null) {
            throw new RuntimeException("This link points to a file, not a folder");
        }

        FolderEntity folder = link.getFolder();
        List<FileDTOs.FileResponse> files = fileService.getFiles(folder.getId());
        List<FolderDTOs.FolderResponse> subFolders = folderService.getFolders(folder.getId());

        SharedFolderContentsResponse response = new SharedFolderContentsResponse();
        response.setFolderId(folder.getId());
        response.setFolderName(folder.getFolderName());
        response.setFullPath(folder.getFullPath());
        response.setPermission(link.getPermission());
        response.setFiles(files);
        response.setSubFolders(subFolders);
        return response;
    }


}