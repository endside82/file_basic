package com.endside.file.manage.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3PathInfo {

    private final String bucket;
    private final String typePath;
    private final String addPath;
}
