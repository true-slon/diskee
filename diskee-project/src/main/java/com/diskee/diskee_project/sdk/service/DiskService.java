package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import com.diskee.diskee_project.api.request.UploadedFile;
import org.springframework.core.io.ByteArrayResource;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiskService{

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @SneakyThrows
    public UploadedFile uploadFile(MultipartFile file) {
        String key = generateKey(file.getOriginalFilename());
        String bucket = s3Properties.getBucket();

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("File uploaded to S3: bucket={}, key={}", bucket, key);

        return UploadedFile.builder()
                .key(key)
                .filename(extractFilenameFromKey(key))
                .originalFilename(file.getOriginalFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .extension(getExtension(file.getOriginalFilename()))
                .build();
    }

    @SneakyThrows
    public Resource getFileFromKey(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(key)
                .build();

        byte[] content = s3Client.getObjectAsBytes(getRequest).asByteArray();
        return new ByteArrayResource(content);
    }

    public boolean deleteByKey(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from S3: key={}", key);
            return true;
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: key={}, error={}", key, e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    private String generateKey(String originalFilename) {
        return UUID.randomUUID() + "/" + originalFilename;
    }

    private String extractFilenameFromKey(String key) {
        return key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }
}