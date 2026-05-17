package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.api.exception.FileNotFoundProblem;
import com.diskee.diskee_project.api.request.CreateSharedLinkRequest;
import com.diskee.diskee_project.api.response.SharedLinkInfoResponse;
import com.diskee.diskee_project.api.response.SharedLinkResponse;
import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.FolderEntity;
import com.diskee.diskee_project.sdk.data.SharedLinkEntity;
import com.diskee.diskee_project.sdk.data.repo.FileRepo;
import com.diskee.diskee_project.sdk.data.repo.FolderRepo;
import com.diskee.diskee_project.sdk.data.repo.SharedLinkRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SharedLinkService {

    private final SharedLinkRepo sharedLinkRepo;
    private final FileRepo fileRepo;
    private final FolderRepo folderRepo;
    private final CurrentUserService currentUserService;

    @Transactional
    public SharedLinkResponse createForFile(Long fileId, CreateSharedLinkRequest request) {
        FileEntity file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundProblem(fileId));

        SharedLinkEntity link = SharedLinkEntity.builder()
                .file(file)
                .token(generateToken())
                .permission(request.getPermission() != null ? request.getPermission() : "view")
                .expiresAt(request.getExpiresAt())
                .build();

        return toResponse(sharedLinkRepo.save(link));
    }

    @Transactional
    public SharedLinkResponse createForFolder(Long folderId, CreateSharedLinkRequest request) {
        FolderEntity folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found: " + folderId));

        SharedLinkEntity link = SharedLinkEntity.builder()
                .folder(folder)
                .token(generateToken())
                .permission(request.getPermission() != null ? request.getPermission() : "view")
                .expiresAt(request.getExpiresAt())
                .build();

        return toResponse(sharedLinkRepo.save(link));
    }

    @Transactional(readOnly = true)
    public SharedLinkInfoResponse resolveToken(String token) {
        SharedLinkEntity link = sharedLinkRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link not found"));

        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Link expired");
        }

        SharedLinkInfoResponse response = new SharedLinkInfoResponse();
        response.setToken(link.getToken());
        response.setPermission(link.getPermission());
        response.setExpiresAt(link.getExpiresAt());
        response.setDownloadCount(link.getDownloadCount());

        if (link.getFile() != null) {
            response.setType("file");
            response.setTargetId(link.getFile().getId());
            response.setName(link.getFile().getFileName());
        } else if (link.getFolder() != null) {
            response.setType("folder");
            response.setTargetId(link.getFolder().getId());
            response.setName(link.getFolder().getFolderName());
        }

        return response;
    }

    @Transactional
    public void incrementDownloadCount(String token) {
        sharedLinkRepo.findByToken(token).ifPresent(link -> {
            link.setDownloadCount(link.getDownloadCount() + 1);
            sharedLinkRepo.save(link);
        });
    }

    @Transactional
    public void delete(UUID linkId) {
        sharedLinkRepo.deleteById(linkId);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private SharedLinkResponse toResponse(SharedLinkEntity entity) {
        SharedLinkResponse response = new SharedLinkResponse();
        response.setId(entity.getId());
        response.setToken(entity.getToken());
        response.setPermission(entity.getPermission());
        response.setExpiresAt(entity.getExpiresAt());
        response.setDownloadCount(entity.getDownloadCount());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}