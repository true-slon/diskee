package com.diskee.diskee_project.controller;

import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileEntity> upload(@RequestParam("file") MultipartFile file) {
        FileEntity saved = fileService.create(file);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        FileEntity fileEntity = fileService.findById(fileId);
        Resource resource = fileService.getFileByKey(fileEntity.getStorageObjectKey());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileEntity.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }
}