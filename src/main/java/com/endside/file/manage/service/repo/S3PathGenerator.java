package com.endside.file.manage.service.repo;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.manage.constant.CategoryType;
import com.endside.file.manage.constant.FileType;
import com.endside.file.manage.service.S3AccessInfo;
import com.endside.file.manage.service.S3PathInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class S3PathGenerator {

    @Value("${amazon.image.path}")
    private String imagePath;

    @Value("${amazon.video.path}")
    private String videoPath;

    @Value("${amazon.common.path}")
    private String filePath;

    @Value("${amazon.s3.profile-bucket.name}")
    private String amazonS3ProfileBucketName;

    @Value("${amazon.s3.resource-bucket.name}")
    private String amazonS3ResourceBucketName;


    public String getBucketName(CategoryType categoryType) {
        switch (categoryType) {
            case PROFILE: return amazonS3ProfileBucketName;
            case RESOURCE: return amazonS3ResourceBucketName;
            default:
                throw new InvalidParameterException(ErrorCode.INVALID_CATEGORY);
        }
    }

    /**
     * 이미지 저장 경로 생성  (카테고리에 따라 각기 다른 경로 )*
     * @return
     */
    public S3AccessInfo pathGenerator(CategoryType categoryType, FileType fileType, String accountHex) throws Exception {
        S3PathInfo s3PathInfo = getS3PathInfo(categoryType, fileType, accountHex);
        String s3Path = generateS3Path(categoryType, s3PathInfo.getTypePath(), s3PathInfo.getAddPath());

        return new S3AccessInfo(s3PathInfo.getBucket(), s3Path);
    }

    private S3PathInfo getS3PathInfo(CategoryType categoryType, FileType fileType, String accountHex) {
        String typePath;
        String addPath;
        String bucket;
        switch (categoryType) {
            case PROFILE:
                typePath = fileType.getTypeString();
                addPath = profileAddPath(accountHex);
                bucket = getBucketName(categoryType);
                break;
            case RESOURCE:
                typePath = fileType.getTypeString();
                addPath = appPathSimpleDate();
                bucket = getBucketName(categoryType);
                break;

            default:
                throw new InvalidParameterException(ErrorCode.INVALID_CATEGORY);
        }

        return new S3PathInfo(bucket, typePath, addPath);
    }

    private String generateS3Path(CategoryType categoryType, String typePath, String addPath) {
        String s3Path = "";
        switch (categoryType) {
            case PROFILE:
            case RESOURCE:
                s3Path = typePath + "/" + addPath;
                break;
            case UNKNOWN:
                s3Path = addPath + typePath + "/";
                break;
            default:
                throw new InvalidParameterException(ErrorCode.INVALID_CATEGORY);
        }

        return s3Path;
    }

    private String appPathAccount(String accountHex) {
        return accountHex + "/";
    }

    private String appPathSimpleDate() {
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();
        String today = now.format(dateformat);
        return today + "/";
    }

    private String profileAddPath(String accountHex) {
        return accountHex + "/";
    }

    private String groupAddPath() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/";
    }

    private String adItemAddPath(String accountHex) {
        return accountHex + "/";
    }

    private String aeItemAddPath() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/";
    }

    private String snsAddPath(String accountHex) {
        return "sns/" + accountHex + "/";
    }

    private String  appPathDateAccount(String accountHex) {
        DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter month = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter day = DateTimeFormatter.ofPattern("dd");
        LocalDate now = LocalDate.now();
        String yyyy = now.format(year);
        String mm = now.format(month);
        String dd = now.format(day);
        return yyyy + "/" + mm + "/" + dd + "/" + accountHex;
    }

}
