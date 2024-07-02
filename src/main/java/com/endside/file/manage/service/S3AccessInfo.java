package com.endside.file.manage.service;

import lombok.Data;

@Data
public class S3AccessInfo {
    private String bucket;
    private String path;

    public S3AccessInfo(String bucket, String path) {
        this.bucket = bucket;
        this.path = path;
    }
}
