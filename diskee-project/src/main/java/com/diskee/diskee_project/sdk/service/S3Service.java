package com.diskee.diskee_project.sdk.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.diskee.diskee_project.api.request.UploadedFile;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3Service {

    private final MinioClient minioClient;

    @Value("${s3.bucket}")
    private String bucket;

    public S3Service(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @SneakyThrows
    public UploadedFile uploadFile(MultipartFile file) {
        String objectName = generateKey(file.getOriginalFilename());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        log.info("File uploaded to MinIO: bucket={}, key={}", bucket, objectName);

        return UploadedFile.builder()
                .key(objectName)
                .filename(extractFilenameFromKey(objectName))
                .originalFilename(file.getOriginalFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .extension(getExtension(file.getOriginalFilename()))
                .build();
    }

    @SneakyThrows
    public Resource getFileFromKey(String key) {
        InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .build()
        );
        byte[] content = is.readAllBytes();
        is.close();
        return new ByteArrayResource(content);
    }

    @SneakyThrows
    public boolean deleteByKey(String key) {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .build()
        );
        log.info("File deleted from MinIO: key={}", key);
        return true;
    }

    private String generateKey(String originalFilename) {
        return UUID.randomUUID() + "/" + originalFilename;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }

    private String extractFilenameFromKey(String key) {
        return key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
    }
}