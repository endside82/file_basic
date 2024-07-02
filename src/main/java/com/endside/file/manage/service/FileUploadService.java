package com.endside.file.manage.service;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.AccessDeniedException;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.error.exception.NotFoundException;
import com.endside.file.manage.constant.CategoryType;
import com.endside.file.manage.constant.FileConfig;
import com.endside.file.manage.constant.FileType;
import com.endside.file.manage.constant.PublicImages;
import com.endside.file.manage.dto.FileBucket;
import com.endside.file.manage.service.repo.CommonFileRepo;
import com.endside.file.manage.service.repo.S3PathGenerator;
import com.endside.file.manage.service.repo.S3PathValidator;
import com.endside.file.manage.response.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {


    private final PublicImages publicImages;

    private final CommonFileRepo commonFileRepo;

    private final S3PathGenerator s3PathGenerator;
    private final S3PathValidator s3PathValidator;

    public FileUploadService( PublicImages publicImages, CommonFileRepo commonFileRepo,
                              S3PathGenerator s3PathGenerator, S3PathValidator s3PathValidator) {
        this.publicImages = publicImages;
        this.commonFileRepo = commonFileRepo;
        this.s3PathGenerator = s3PathGenerator;
        this.s3PathValidator = s3PathValidator;
    }

    /**
     * Upload Image File
     * @param file File
     * @param category Category Name
     * @param type 파일타입  image / video / none
     * @param accountHex User Index
     * @param md5 md5 checksum
     * @return Path of updated file
     * @throws Exception
     */
    public String uploadFile( MultipartFile file, String category, String type, String accountHex, String md5) throws Exception {
        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);
        FileType fileType = FileType.getFileTypeByTypePath(type);
        // 이미지 저장 경로 생성
        S3AccessInfo accessInfo = s3PathGenerator.pathGenerator(categoryType, fileType, accountHex);
        // 이미지 파일 업로드
        return commonFileRepo.uploadFile(accessInfo.getBucket(), accessInfo.getPath(), file, md5);
    }


    /**
     * Upload none media files
     * @param files Files
     * @param category Category Name
     * @param type 파일타입  image / video / none
     * @param accountHex User Index
     * @return Paths of uploaded files
     * @throws Exception 예외
     */
    public List<FileUploadResponse> uploadFiles(MultipartFile[] files, String category, String type, String accountHex) throws Exception {
        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);
        FileType fileType = FileType.getFileTypeByTypePath(type);
        // 이미지 저장 경로 생성
        S3AccessInfo accessInfo = s3PathGenerator.pathGenerator(categoryType, fileType, accountHex);
        // 이미지 파일들 업로드
        return commonFileRepo.uploadFiles(accessInfo.getBucket(), accessInfo.getPath(), files);
    }

    /**
     * Get Image File
     * @param category     Category Name
     * @param path         File path to get
     * @return File Bucket include file information
     * @throws Exception
     */
    public FileBucket getFile(String category, String path, String type, String accountHex) throws Exception {

        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);

        if (!s3PathValidator.isValid(categoryType, path)) {
            throw new InvalidParameterException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }

        return commonFileRepo.getFile(s3PathGenerator.getBucketName(categoryType), path);
    }

    /**
     * Delete Image File
     * @param category  Category Name
     * @param path      File path to delete
     * @return Not required
     * @throws Exception
     */
    public void deleteFile(String category, String path) throws NotFoundException {

        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);

        if (!s3PathValidator.isValid(categoryType, path)) {
            throw new InvalidParameterException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }

        commonFileRepo.deleteFile(s3PathGenerator.getBucketName(categoryType), path);
    }

    public String getSignedUploadPath( String category, String type, String accountHex, String formatName) throws Exception {
        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);
        FileType fileType = FileType.getFileTypeByTypePath(type);
        // 이미지 저장 경로 생성
        S3AccessInfo accessInfo = s3PathGenerator.pathGenerator(categoryType, fileType, accountHex);
        String saveFileName = accessInfo.getPath() + UUID.randomUUID() + "." + formatName;
        return commonFileRepo.getSignedUploadPath(accessInfo.getBucket(), saveFileName, FileConfig.getTimeByType(type));
    }

    public String getSignedPath( String category, String path, String type, String accountHex) {
        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);
        return commonFileRepo.getSignedPath(s3PathGenerator.getBucketName(categoryType), path, FileConfig.getTimeByType(type));
    }

    /**
     * Get Image File
     * @param path         File path to get
     * @return File Bucket include file information
     * @throws Exception
     */
    public FileBucket getPublicFile(String path) throws Exception {
        if (!StringUtils.hasText(path)) {
            throw new InvalidParameterException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }
        if (publicImages.checkIsPublicResource(path)) {
            throw new AccessDeniedException(ErrorCode.RESOURCE_NOT_PUBLIC);
        }
        CategoryType categoryType = CategoryType.getStorageTypeByCategory("resource");
        return commonFileRepo.getFile( s3PathGenerator.getBucketName(categoryType) , path );
    }


}
