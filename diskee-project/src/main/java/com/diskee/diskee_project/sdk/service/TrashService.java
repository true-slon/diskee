package com.diskee.diskee_project.sdk.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.diskee.diskee_project.dto.TrashItemDTO;
import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.FolderEntity;
import com.diskee.diskee_project.sdk.data.TrashBinEntity;
import com.diskee.diskee_project.sdk.data.repo.FileRepo;
import com.diskee.diskee_project.sdk.data.repo.FolderRepo;
import com.diskee.diskee_project.sdk.data.repo.TrashBinRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrashService {

    private final TrashBinRepo trashBinRepo;
    private final FileRepo fileRepo;
    private final FolderRepo folderRepo;
    private final S3Service diskService;
    private final CurrentUserService currentUserService;
    private final CacheManager cacheManager;                     

    @Transactional
    public TrashBinEntity moveFileToTrash(Long fileId) {
        FileEntity file = fileRepo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Файл не найден: " + fileId));

        if (file.isDeleted()) {
            throw new RuntimeException("Файл уже в корзине");
        }

        file.setDeleted(true);
        fileRepo.save(file);

        TrashBinEntity trash = TrashBinEntity.builder()
                .user(currentUserService.getUser())
                .file(file)
                .originalPath(file.getFileName())
                .deletedAt(Instant.now())
                .autoDeleteAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();

        TrashBinEntity saved = trashBinRepo.save(trash);

        Objects.requireNonNull(cacheManager.getCache("file_metadata")).evict(fileId);
        Objects.requireNonNull(cacheManager.getCache("file_folder_contents")).clear();

        log.info("Файл перемещён в корзину: {}", file.getFileName());
        return saved;
    }

    @Transactional
    public void moveFolderToTrash(Long folderId) {
        FolderEntity folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Папка не найдена: " + folderId));

        folder.setDeletedAt(Instant.now());
        folderRepo.save(folder);

        List<FileEntity> files = fileRepo.findAllByParentFolderIdAndIsDeletedFalse(folderId);
        for (FileEntity file : files) {
            moveFileToTrash(file.getId());
        }

        List<FolderEntity> subFolders = folderRepo.findAllByParentFolderIdAndDeletedAtIsNull(folderId);
        for (FolderEntity sub : subFolders) {
            moveFolderToTrash(sub.getId());
        }

        TrashBinEntity trash = TrashBinEntity.builder()
                .user(currentUserService.getUser())
                .folder(folder)
                .originalPath(folder.getFullPath())
                .deletedAt(Instant.now())
                .autoDeleteAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();

        trashBinRepo.save(trash);

        Objects.requireNonNull(cacheManager.getCache("file_folder_contents")).clear();

        log.info("Папка перемещена в корзину: {}", folder.getFolderName());
    }

    @Transactional(readOnly = true)
    public List<TrashItemDTO> getTrashContents() {
        Long userId = currentUserService.getUser().getId();
        return trashBinRepo.findAllByUserId(userId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private TrashItemDTO toDto(TrashBinEntity entity) {
        return TrashItemDTO.builder()
            .id(entity.getId())
            .itemType(entity.getFile() != null ? "file" : "folder")
            .name(entity.getFile() != null ? entity.getFile().getFileName() 
                                        : entity.getFolder().getFolderName())
            .originalPath(entity.getOriginalPath())
            .size(entity.getFile() != null ? entity.getFile().getFileSizeBytes() : null)
            .deletedAt(entity.getDeletedAt())
            .autoDeleteAt(entity.getAutoDeleteAt())
            .build();
    }
    
    @Transactional
    public void restoreFromTrash(Long trashId) {
        TrashBinEntity trash = trashBinRepo.findById(trashId).orElseThrow();

        if (trash.getFolder() != null) {
            restoreFolderRecursively(trash.getFolder());
        }
        if (trash.getFile() != null) {
            trash.getFile().setDeleted(false);
            fileRepo.save(trash.getFile());
        }
        trashBinRepo.delete(trash);

        Long userId = currentUserService.getUser().getId();
        Cache cache = cacheManager.getCache("file_folder_contents");
        if (cache != null) {
            cache.evict(userId + "-root");
        }
    }

    private void restoreFolderRecursively(FolderEntity folder) {
        folder.setDeletedAt(null);
        folderRepo.save(folder);
        
        List<FileEntity> files = fileRepo.findAllByParentFolderId(folder.getId());
        for (FileEntity file : files) {
            file.setDeleted(false);
            fileRepo.save(file);
        }
        
        List<FolderEntity> subs = folderRepo.findAllByParentFolderId(folder.getId());
        for (FolderEntity sub : subs) {
            restoreFolderRecursively(sub);
        }
    }

    @Transactional
    public void permanentDelete(Long trashId) {
        TrashBinEntity trash = trashBinRepo.findById(trashId)
                .orElseThrow(() -> new RuntimeException("Запись в корзине не найдена: " + trashId));

        Long fileId = null;

        if (trash.getFile() != null) {
            FileEntity file = trash.getFile();
            fileId = file.getId();
            diskService.deleteByKey(file.getStorageObjectKey());
            if (file.getPreviewObjectKey() != null) {
                diskService.deleteByKey(file.getPreviewObjectKey());
            }
            trashBinRepo.delete(trash);
            fileRepo.delete(file);
            log.info("Файл удалён навсегда: {}", file.getFileName());
        }

        if (trash.getFolder() != null) {
            trashBinRepo.delete(trash);
            folderRepo.delete(trash.getFolder());
            log.info("Папка удалена навсегда: {}", trash.getFolder().getFolderName());
        }

        if (fileId != null) {
            Objects.requireNonNull(cacheManager.getCache("file_metadata")).evict(fileId);
        }
        Objects.requireNonNull(cacheManager.getCache("file_folder_contents")).clear();
        Objects.requireNonNull(cacheManager.getCache("storage_info")).clear();
    }

    @Transactional
    public void clearTrash() {
        Long userId = currentUserService.getUser().getId();
        List<TrashBinEntity> allTrash = trashBinRepo.findAllByUserId(userId);

        for (TrashBinEntity trash : allTrash) {
            if (trash.getFile() != null) {
                diskService.deleteByKey(trash.getFile().getStorageObjectKey());
                trashBinRepo.delete(trash);
                fileRepo.delete(trash.getFile());
            }
            if (trash.getFolder() != null) {
                trashBinRepo.delete(trash);
                folderRepo.delete(trash.getFolder());
            }
        }

        Objects.requireNonNull(cacheManager.getCache("file_metadata")).clear();
        Objects.requireNonNull(cacheManager.getCache("file_folder_contents")).clear();
        Objects.requireNonNull(cacheManager.getCache("storage_info")).clear();

        log.info("Корзина очищена для пользователя {}", userId);
    }

    @Transactional
    public void autoCleanExpired() {
        List<TrashBinEntity> expired = trashBinRepo.findAllByAutoDeleteAtBefore(Instant.now());

        for (TrashBinEntity trash : expired) {
            if (trash.getFile() != null) {
                diskService.deleteByKey(trash.getFile().getStorageObjectKey());
                trashBinRepo.delete(trash);
                fileRepo.delete(trash.getFile());
            }
            if (trash.getFolder() != null) {
                trashBinRepo.delete(trash);
                folderRepo.delete(trash.getFolder());
            }
        }

        if (!expired.isEmpty()) {
            Objects.requireNonNull(cacheManager.getCache("file_metadata")).clear();
            Objects.requireNonNull(cacheManager.getCache("file_folder_contents")).clear();
            Objects.requireNonNull(cacheManager.getCache("storage_info")).clear();
        }

        log.info("Автоочистка корзины: удалено {} элементов", expired.size());
    }
}