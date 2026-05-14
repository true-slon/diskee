package com.diskee.diskee_project.config;

import lombok.Data;

@Data
public class S3Properties {
    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private boolean pathStyleAccess = false;
}