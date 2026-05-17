package com.diskee.diskee_project.sdk.service;

import java.util.List;
import java.util.stream.Collectors;

import com.diskee.diskee_project.dto.FileDTOs;
import com.diskee.diskee_project.dto.FolderDTOs;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.diskee.diskee_project.dto.FolderDTOs.FolderRequest;
import com.diskee.diskee_project.dto.FolderDTOs.FolderResponse;
import com.diskee.diskee_project.sdk.data.FolderEntity;
import com.diskee.diskee_project.sdk.data.repo.FolderRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderService {

    private final FolderRepo folderRepo;
    private final CurrentUserService currentUserService;
    private final TrashService trashService;
    private final CacheInvalidationService cacheInvalidationService;
    private final FileService fileService;


    @Transactional(readOnly = true)
    public List<FolderResponse> getFolders(Long parentId) {
        List<FolderEntity> folders;

        if (parentId == null) {
            folders = folderRepo.findByUserIdAndParentFolderIsNullAndDeletedAtIsNull(
                    currentUserService.getUser().getId()
            );
        } else {
            folders = folderRepo.findByParentFolderIdAndUserIdAndDeletedAtIsNull(
                    parentId, currentUserService.getUser().getId()
            );
        }
        return folders.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public FolderResponse create(FolderRequest request) {
        FolderEntity parent = null;
        if (request.getParentFolderId() != null) {
            parent = folderRepo.findById(request.getParentFolderId()).orElse(null);
        }

        String fullPath = (parent != null)
                ? parent.getFullPath() + "/" + request.getFolderName()
                : "/" + request.getFolderName();

        FolderEntity folder = FolderEntity.builder()
                .user(currentUserService.getUser())
                .parentFolder(parent)
                .folderName(request.getFolderName())
                .fullPath(fullPath)
                .build();

        folder = folderRepo.saveAndFlush(folder);
        log.info("Folder created: {}", folder.getFolderName());

        // Инвалидация кеша родительской папки (или root)
        cacheInvalidationService.evictFolderContents(request.getParentFolderId());
        return toResponse(folder);
    }

    @Transactional
    public FolderResponse rename(Long folderId, String newName) {
        FolderEntity folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found: " + folderId));

        Long parentId = folder.getParentFolder() != null ? folder.getParentFolder().getId() : null;

        String oldPath = folder.getFullPath();
        String newPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + newName;

        folder.setFolderName(newName);
        folder.setFullPath(newPath);
        folder = folderRepo.saveAndFlush(folder);
        log.info("Folder renamed: {} -> {}", oldPath, newPath);

        // Инвалидация родительской папки, потому что изменилось имя в её списке
        cacheInvalidationService.evictFolderContents(parentId);
        return toResponse(folder);
    }

    @Transactional
    public FolderResponse move(Long folderId, Long targetFolderId) {
        FolderEntity folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found: " + folderId));

        if (folderId.equals(targetFolderId)) {
            throw new RuntimeException("Cannot move folder into itself");
        }

        // Запоминаем старого родителя до перемещения
        Long oldParentId = folder.getParentFolder() != null ? folder.getParentFolder().getId() : null;

        FolderEntity target = null;
        if (targetFolderId != null) {
            target = folderRepo.findById(targetFolderId)
                    .orElseThrow(() -> new RuntimeException("Target folder not found: " + targetFolderId));

            FolderEntity current = target;
            while (current != null) {
                if (current.getId().equals(folderId)) {
                    throw new RuntimeException("Cannot move folder into its own subfolder");
                }
                current = current.getParentFolder();
            }
        }

        String newPath = (target != null)
                ? target.getFullPath() + "/" + folder.getFolderName()
                : "/" + folder.getFolderName();

        folder.setParentFolder(target);
        folder.setFullPath(newPath);
        folder = folderRepo.saveAndFlush(folder);
        log.info("Folder moved: {} -> {}", folder.getFolderName(), newPath);

        // Инвалидация старой и новой родительской папок
        cacheInvalidationService.evictFolderContents(oldParentId);
        cacheInvalidationService.evictFolderContents(targetFolderId);
        return toResponse(folder);
    }

    @Transactional
    public FolderResponse copy(Long folderId, Long targetFolderId) {
        FolderEntity original = folderRepo.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found: " + folderId));

        FolderEntity target = null;
        if (targetFolderId != null) {
            target = folderRepo.findById(targetFolderId)
                    .orElseThrow(() -> new RuntimeException("Target folder not found: " + targetFolderId));
        }

        String newName = original.getFolderName() + " (копия)";
        String newPath = (target != null)
                ? target.getFullPath() + "/" + newName
                : "/" + newName;

        FolderEntity copy = FolderEntity.builder()
                .user(currentUserService.getUser())
                .parentFolder(target)
                .folderName(newName)
                .fullPath(newPath)
                .build();

        copy = folderRepo.saveAndFlush(copy);
        log.info("Folder copied: {} -> {}", original.getFolderName(), newPath);

        // Инвалидация целевой папки (или root)
        cacheInvalidationService.evictFolderContents(targetFolderId);
        return toResponse(copy);
    }

    @Transactional
    public void delete(Long folderId) {
        // Определяем родителя до перемещения в корзину (если потребуется инвалидация)
        FolderEntity folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found: " + folderId));
        Long parentId = folder.getParentFolder() != null ? folder.getParentFolder().getId() : null;

        trashService.moveFolderToTrash(folderId);

        // Инвалидация родительской папки, из которой исчезла папка
        cacheInvalidationService.evictFolderContents(parentId);
    }

    @Transactional
    @Cacheable(value = "folder_contents", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '-' + (#parentId != null ? #parentId : 'root')")
    public FolderDTOs.FolderContentsResponse getContents(Long id) {
        List<FolderResponse> folders = getFolders(id);
        List<FileDTOs.FileResponse> files = fileService.getFiles(id);

        FolderDTOs.FolderContentsResponse response = new FolderDTOs.FolderContentsResponse();
        response.setFolders(folders);
        response.setFiles(files);
        response.setTotalFolders(folders.size());
        response.setTotalFiles(files.size());
        return response;
    }

    private FolderResponse toResponse(FolderEntity entity) {
        FolderResponse response = new FolderResponse();
        response.setId(entity.getId());
        response.setFolderName(entity.getFolderName());
        response.setFullPath(entity.getFullPath());
        response.setParentFolderId(entity.getParentFolder() != null ? entity.getParentFolder().getId() : null);
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}