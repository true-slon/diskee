package com.diskee.diskee_project.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.diskee.diskee_project.dto.FileDTOs;
import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.TrashBinEntity;
import com.diskee.diskee_project.sdk.service.CurrentUserService;
import com.diskee.diskee_project.sdk.service.FileService;
import com.diskee.diskee_project.sdk.service.TrashService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final TrashService trashService;
    private final CurrentUserService currentUserService;
    
    // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<FileEntity> upload(@RequestParam("file") MultipartFile file) {
    //     FileEntity saved = fileService.create(file);
    //     return ResponseEntity.ok(saved);
    // }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDTOs.FileResponse> upload(@RequestParam("file") MultipartFile file, @RequestParam(value = "parentFolderId", required = false) Long parentId) {
        FileEntity saved = fileService.create(file, parentId);
        return ResponseEntity.ok(fileService.toFileResponse(saved));  
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileDTOs.FileResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String category) {
        Long userId = currentUserService.getUser().getId();
        return ResponseEntity.ok(fileService.searchFiles(userId, q, folderId, category));
    }


    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        FileEntity fileEntity = fileService.findById(fileId, true);
        Resource resource = fileService.getFileByKey(fileEntity.getStorageObjectKey());
        
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(fileEntity.getMimeType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        
        String encodedFileName = URLEncoder.encode(fileEntity.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }

    // @PatchMapping("/{fileId}/move")
    // public ResponseEntity<FileEntity> moveFile(
    //         @PathVariable Long fileId,
    //         @RequestBody FileDTOs.FileMoveRequest request) {
    //     FileEntity moved = fileService.moveFile(fileId, request.getParentFolderId());
    //     return ResponseEntity.ok(moved);
    // }
    @PatchMapping("/{fileId}/move")
    public ResponseEntity<FileDTOs.FileResponse> moveFile(
            @PathVariable Long fileId,
            @RequestBody FileDTOs.FileMoveRequest request) {
        FileEntity moved = fileService.moveFile(fileId, request.getParentFolderId());
        return ResponseEntity.ok(fileService.toFileResponse(moved));
    }
    @PostMapping("/{fileId}/trash")
    public ResponseEntity<Map<String, Object>> moveToTrash(@PathVariable Long fileId) {
        TrashBinEntity trash = trashService.moveFileToTrash(fileId);
        return ResponseEntity.ok(Map.of(
                "message", "Файл перемещён в корзину",
                "autoDeleteAt", trash.getAutoDeleteAt().toString()
        ));
    }
}