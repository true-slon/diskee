package com.diskee.diskee_project.controller;

import java.util.List;

import jakarta.persistence.Cacheable;
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

import com.diskee.diskee_project.dto.FileDTOs.FileResponse;
import com.diskee.diskee_project.dto.FolderDTOs.FolderContentsResponse;
import com.diskee.diskee_project.dto.FolderDTOs.FolderRequest;
import com.diskee.diskee_project.dto.FolderDTOs.FolderResponse;
import com.diskee.diskee_project.sdk.service.FileService;
import com.diskee.diskee_project.sdk.service.FolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<FolderContentsResponse> getContents(
            @RequestParam(required = false) Long parentId) {

        return ResponseEntity.ok(folderService.getContents(parentId));
    }

    @PostMapping
    public ResponseEntity<FolderResponse> create(@Valid @RequestBody FolderRequest request) {
        FolderResponse folder = folderService.create(request);
        return ResponseEntity.ok(folder);
    }

    @PatchMapping("/{folderId}")
    public ResponseEntity<FolderResponse> rename(
            @PathVariable Long folderId,
            @RequestBody FolderRequest request) {
        FolderResponse folder = folderService.rename(folderId, request.getFolderName());
        return ResponseEntity.ok(folder);
    }

    @PatchMapping("/{folderId}/move")
    public ResponseEntity<FolderResponse> move(
            @PathVariable Long folderId,
            @RequestBody FolderRequest request) {
        FolderResponse folder = folderService.move(folderId, request.getParentFolderId());
        return ResponseEntity.ok(folder);
    }

    @PostMapping("/{folderId}/copy")
    public ResponseEntity<FolderResponse> copy(
            @PathVariable Long folderId,
            @RequestBody FolderRequest request) {
        FolderResponse folder = folderService.copy(folderId, request.getParentFolderId());
        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(@PathVariable Long folderId) {
        folderService.delete(folderId);
        return ResponseEntity.noContent().build();
    }
}