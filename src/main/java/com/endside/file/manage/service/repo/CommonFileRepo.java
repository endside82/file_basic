package com.endside.file.manage.service.repo;

import com.endside.file.config.error.exception.NotFoundException;
import com.endside.file.manage.dto.FileBucket;
import com.endside.file.manage.response.FileUploadResponse;
import com.endside.file.util.AmazonS3Util;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.ArrayList;

@Slf4j
@Component
public class CommonFileRepo {
    @Resource
    AmazonS3Util amazonS3Util;

    public String uploadFile(String bucketName, String path, MultipartFile file, String md5) throws Exception {
        return amazonS3Util.uploadMultipartFile(bucketName, path, file, md5);
    }

    public ArrayList<FileUploadResponse> uploadFiles(String bucketName, String path, MultipartFile[] files) throws Exception {
        ArrayList<FileUploadResponse> fileUploadResponses = new ArrayList<>();
        String filePath;
        FileUploadResponse  tempResponse;
        for (MultipartFile file : files) {
            tempResponse = new FileUploadResponse();
            filePath = amazonS3Util.uploadMultipartFile(bucketName, path, file, null);
            tempResponse.setPath(filePath);
            fileUploadResponses.add(tempResponse);
        }
        return fileUploadResponses;
    }

    public void deleteFile(String bucketName, String path) throws NotFoundException {
        amazonS3Util.deleteFile(bucketName, path);
    }

    public FileBucket getFile(String bucketName, String path) throws Exception {
        return amazonS3Util.downloadFile(bucketName, path);
    }

    public String getSignedUploadPath(String bucketName, String path, int time) {
        return amazonS3Util.signBucket(bucketName, path, time);
    }

    public String getSignedPath(String bucketName, String path, int time) {
        return amazonS3Util.signedUrl(bucketName, path, time);
    }
}
