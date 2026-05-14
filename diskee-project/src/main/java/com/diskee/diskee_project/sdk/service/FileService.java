package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.api.exception.FileNotFoundProblem;
import com.diskee.diskee_project.api.request.UploadedFile;
import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.repo.FileRepo;
import com.diskee.diskee_project.sdk.data.spec.FileSpecification;
import com.diskee.diskee_project.utils.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final FileRepo fileRepository;
    private final S3Service diskService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public FileEntity findById(Long id) {
        return findById(id, false);
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

    @Transactional
    public FileEntity create(MultipartFile file) {
        UploadedFile uploadedFile = diskService.uploadFile(file);

        FileEntity fileEntity = FileEntity.builder()
                .user(currentUserService.getUser())
                .storageObjectKey(uploadedFile.getKey())
                .fileName(uploadedFile.getFilename())
                .fileExtension(uploadedFile.getExtension())
                .mimeType(uploadedFile.getContentType())
                .fileSizeBytes(uploadedFile.getSize())
                .isDeleted(false)
                .build();

        fileRepository.saveAndFlush(fileEntity);
        log.info("File entity stored: {}", fileEntity);
        return fileEntity;
    }

    @Transactional(readOnly = true)
    public Resource getFileByKey(String key) {
        return diskService.getFileFromKey(key);
    }

    @Transactional
    public void delete(Long fileId) {
        FileEntity file = fileRepository.findById(fileId).orElse(null);
        if (file == null) return;

        file.setDeleted(true);
        fileRepository.save(file);
        diskService.deleteByKey(file.getStorageObjectKey());
    }

    @Transactional
    public void delete(List<Long> fileIds) {
        List<FileEntity> files = fileRepository.findAllById(fileIds);
        files.forEach(file -> {
            file.setDeleted(true);
            diskService.deleteByKey(file.getStorageObjectKey());
        });
        fileRepository.saveAll(files);
    }

    @SneakyThrows
    @Transactional
    public FileEntity createInDb(String storageKey) {
        Resource resource = diskService.getFileFromKey(storageKey);
        String filename = extractFilenameFromKey(storageKey);

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
        log.info("File entity stored from S3: {}", fileEntity.getFileName());
        return fileEntity;
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
        return fileEntity;
    }

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
}