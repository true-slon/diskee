package com.diskee.diskee_project.controller;

import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.diskee.diskee_project.sdk.service.S3Service;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

   @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
    try {
        s3Service.uploadFile(file.getOriginalFilename(), file);
        return ResponseEntity.ok("Файл загружен: " + file.getOriginalFilename());
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
    }
}

    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String fileName) {
        try {
            InputStream stream = s3Service.downloadFile(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> delete(@PathVariable String fileName) {
        try {
            s3Service.deleteFile(fileName);
            return ResponseEntity.ok("Файл удалён: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }
}