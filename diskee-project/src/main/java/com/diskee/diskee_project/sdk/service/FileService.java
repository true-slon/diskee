package com.diskee.diskee_project.sdk.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.diskee.diskee_project.api.exception.FileNotFoundProblem;
import com.diskee.diskee_project.api.request.UploadedFile;
import com.diskee.diskee_project.dto.FileDTOs;
import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.FolderEntity;
import com.diskee.diskee_project.sdk.data.repo.FileRepo;
import com.diskee.diskee_project.sdk.data.repo.FolderRepo;
import com.diskee.diskee_project.sdk.data.spec.FileSpecification;
import com.diskee.diskee_project.utils.EntityUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileRepo fileRepository;
    private final S3Service diskService;
    private final CurrentUserService currentUserService;
    private final FolderRepo folderRepo;
    private final CacheInvalidationService cacheInvalidationService;
    private final FolderService folderService;

    @Cacheable(value = "file_metadata", key = "#id")
    @Transactional(readOnly = true)
    public FileEntity findById(Long id) {
        return findById(id, false);
    }

    //@Cacheable(value = "folder_contents", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + '-' + (#parentFolderId != null ? #parentFolderId : 'root')")
    @Transactional(readOnly = true)
    public List<FileDTOs.FileResponse> getFiles(Long parentFolderId) {
        List<FileEntity> files;
        if (parentFolderId == null) {
            files = fileRepository.findByUserIdAndParentFolderIsNullAndIsDeletedFalse(
                    currentUserService.getUser().getId()
            );
        } else {
            files = fileRepository.findByParentFolderIdAndUserIdAndIsDeletedFalse(
                    parentFolderId, currentUserService.getUser().getId()
            );
        }
        log.info("Куррент юзер" + currentUserService.getUser().getId());
        return files.stream().map(this::toFileResponse).collect(Collectors.toList());
    }

    @Transactional
    public FileEntity create(MultipartFile file, Long parentId) {
        UploadedFile uploadedFile = diskService.uploadFile(file);
        FolderEntity folder = null;
        if (parentId != null) {
            folder = folderRepo.findById(parentId).orElseThrow();
        }
        FileEntity fileEntity = FileEntity.builder()
                .user(currentUserService.getUser())
                .parentFolder(folder)
                .storageObjectKey(uploadedFile.getKey())
                .fileName(uploadedFile.getFilename())
                .fileExtension(uploadedFile.getExtension())
                .mimeType(uploadedFile.getContentType())
                .fileSizeBytes(uploadedFile.getSize())
                .isDeleted(false)
                .build();

        fileRepository.saveAndFlush(fileEntity);
        log.info("File entity stored: {}", fileEntity);

        // Файл создаётся в корне – инвалидируем корень
        cacheInvalidationService.evictFolderContents(parentId);
        return fileEntity;
    }

    @Transactional
    public void delete(Long fileId) {
        FileEntity file = fileRepository.findById(fileId).orElse(null);
        if (file == null) return;

        // Запоминаем родителя до удаления
        Long parentId = file.getParentFolder() != null ? file.getParentFolder().getId() : null;

        boolean deletedFromStorage = diskService.deleteByKey(file.getStorageObjectKey());
        if (!deletedFromStorage) {
            throw new RuntimeException("Failed to delete file from storage: " + file.getStorageObjectKey());
        }

        file.setDeleted(true);
        fileRepository.save(file);

        cacheInvalidationService.evictFileMetadata(fileId);
        cacheInvalidationService.evictFolderContents(parentId);
    }

    @Transactional
    public FileEntity moveFile(Long fileId, Long targetFolderId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        Long oldParentId = file.getParentFolder() != null ? file.getParentFolder().getId() : null;

        FolderEntity target = null;
        if (targetFolderId != null) {
            target = folderRepo.findById(targetFolderId)
                    .orElseThrow(() -> new RuntimeException("Folder not found: " + targetFolderId));
        }

        file.setParentFolder(target);
        file = fileRepository.saveAndFlush(file);
        log.info("File moved: {} -> folder {}", file.getFileName(), targetFolderId);

        cacheInvalidationService.evictFolderContents(oldParentId);
        cacheInvalidationService.evictFolderContents(targetFolderId);
        return file;
    }

    @Transactional(readOnly = true)
    public FileEntity findById(Long id, boolean deleted) {
        FileEntity entity = fileRepository.findById(id).orElse(null);
        EntityUtils.checkInconsistency(id, entity, FileNotFoundProblem::new, deleted);
        return entity;
    }

    @Transactional(readOnly = true)
    public List<FileEntity> findAllById(Collection<Long> ids, boolean deleted) {
        List<FileEntity> entities = fileRepository.findAll(Specification
                .where(FileSpecification.deleted(deleted))
                .and(FileSpecification.idIn(ids))
        );
        EntityUtils.checkInconsistency(ids, entities, FileNotFoundProblem::new, deleted);
        return entities;
    }

    @Cacheable(value = "file_metadata", key = "#id")
    @Transactional(readOnly = true)
    public FileDTOs.FileResponse findByIdDto(Long id) {
        return toFileResponse(findById(id, false));
    }

    @Transactional(readOnly = true)
    public Resource getFileByKey(String key) {
        return diskService.getFileFromKey(key);
    }

    @Transactional
    public void delete(List<Long> fileIds) {
        List<FileEntity> files = fileRepository.findAllById(fileIds);
        files.forEach(file -> {
            // Собираем родительские папки для инвалидации
            Long parentId = file.getParentFolder() != null ? file.getParentFolder().getId() : null;
            cacheInvalidationService.evictFolderContents(parentId);
            cacheInvalidationService.evictFileMetadata(file.getId());

            file.setDeleted(true);
            diskService.deleteByKey(file.getStorageObjectKey());
        });
        fileRepository.saveAll(files);
    }

    @SneakyThrows
    @Transactional
    public FileEntity createInDb(String storageKey, String customFileName) {
        Resource resource = diskService.getFileFromKey(storageKey);
        String filename = (customFileName != null) ? customFileName : extractFilenameFromKey(storageKey);

        FileEntity fileEntity = FileEntity.builder()
                .user(currentUserService.getUser())
                .storageObjectKey(storageKey)
                .fileName(filename)
                .fileExtension(getExtension(filename))
                .fileSizeBytes(resource.contentLength())
                .mimeType(detectMimeType(resource))
                .isDeleted(false)
                .build();

        fileRepository.saveAndFlush(fileEntity);
        log.info("File entity stored with custom name: {}", filename);

        // По аналогии с обычным create – инвалидируем корень
        cacheInvalidationService.evictFolderContentsRoot();
        return fileEntity;
    }

    // Вспомогательные методы остаются без изменений...
    private String extractFilenameFromKey(String key) {
        return key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1);
    }

    private String detectMimeType(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return new Tika().detect(is);
        }
    }

    public FileDTOs.FileResponse toFileResponse(FileEntity entity) {
        FileDTOs.FileResponse response = new FileDTOs.FileResponse();
        response.setId(entity.getId());
        response.setFileName(entity.getFileName());
        response.setFileExtension(entity.getFileExtension());
        response.setMimeType(entity.getMimeType());
        response.setFileSizeBytes(entity.getFileSizeBytes());
        response.setParentFolderId(entity.getParentFolder() != null ? entity.getParentFolder().getId() : null);
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}