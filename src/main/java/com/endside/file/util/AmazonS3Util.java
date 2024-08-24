package com.endside.file.util;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.error.exception.NotFoundException;
import com.endside.file.config.error.exception.ServiceUnavailableException;
import com.endside.file.manage.dto.FileBucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class AmazonS3Util {

    @Value("${amazon.region}")
    private String amazonRegionName;

    @Value("${amazon.s3.ec2policy}")
    private Boolean ec2policy;

    @Value("${amazon.s3.credentials.access-key}")
    private String accessKey;

    @Value("${amazon.s3.credentials.secret-key}")
    private String secretKey;

    private S3Client s3Client;

    private AwsCredentialsProvider awsCredentialsProvider;

    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        this.awsCredentialsProvider = getCredentials();
        this.s3Presigner = getS3Presigner();
        this.s3Client = getS3Client();
    }

    /**
     * 자격 증명 취득
     *
     * @return
     */
    private AwsCredentialsProvider getCredentials() {
        AwsCredentialsProvider awsCredentialsProvider;
        if (ec2policy) {
            // ec2 credential 방식 : ec2에 권한을 줘서 파일 서버에 통신할 수 있도록 한다.
            awsCredentialsProvider = InstanceProfileCredentialsProvider.create();
            log.info("ec2 credential" + awsCredentialsProvider.resolveCredentials().toString());
        } else {
            // IAM basic 방식 : local이나 비AWS 환경에서 테스트 가능 하도록 한다.
            if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
                throw new ServiceUnavailableException(ErrorCode.FAILED_UPLOAD_TO_EXTERNAL);
            }
            AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            awsCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        }
        return awsCredentialsProvider;
    }

    /**
     * Amazon S3 client
     *
     * @return
     * @throws Exception
     */
    private S3Client getS3Client() {
        ClientOverrideConfiguration.Builder overrideConfig = ClientOverrideConfiguration.builder();

        return S3Client.builder()
                .forcePathStyle(true)
                .region(Region.of(amazonRegionName))
                .overrideConfiguration(overrideConfig.build())
                .credentialsProvider(this.awsCredentialsProvider)
                .build();
    }

    private S3Presigner getS3Presigner() {
        S3Presigner s3Presigner = S3Presigner.builder()
                .region(Region.of(amazonRegionName))
                .credentialsProvider(this.awsCredentialsProvider)
                .build();
        return s3Presigner;
    }

    public String uploadInputStream(String bucketName, String saveFileName, String contentType, long size,
                                    InputStream inputStream, String md5) throws Exception {
        try {

            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(saveFileName)
                    .acl(ObjectCannedACL.PUBLIC_READ_WRITE)
                    .contentType(contentType)
                    .contentLength(size);
            if (md5 != null) {
                builder.contentMD5(md5);
            }
            PutObjectRequest objectRequest = builder.build();
            PutObjectResponse putObjectResponse = this.s3Client.putObject(objectRequest, RequestBody.fromInputStream(inputStream, size));
        } catch (SdkClientException e) {
            log.error(e.toString());
            log.error("uploadInputStream : SdkClientException (bucketName: {}, saveFileName: {}, contentType: {} )",
                    bucketName, saveFileName, contentType);
            throw new ServiceUnavailableException(ErrorCode.FAILED_UPLOAD_TO_EXTERNAL);
        } catch (S3Exception e) {
            log.error(e.toString());
            log.error("uploadInputStream : S3Exception (bucketName: {}, saveFileName: {}, contentType: {} )",
                    bucketName, saveFileName, contentType);
            throw new ServiceUnavailableException(ErrorCode.FAILED_UPLOAD_TO_EXTERNAL);
        }
        // md
        return saveFileName;
    }

    public String uploadMultipartFile(String bucketName, String path, MultipartFile file, String md5) throws Exception {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new InvalidParameterException(ErrorCode.INVALID_FILE_NAME);
        }
        String formatName = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String saveFileName = path + UUID.randomUUID() + "." + formatName;
        return uploadInputStream(bucketName, saveFileName,
                file.getContentType(), file.getSize(),
                file.getInputStream(), md5);

    }

    public FileBucket downloadFile(String bucketName, String path) throws Exception {
        FileBucket fileBucket = new FileBucket();
        try {
            ResponseBytes<GetObjectResponse> bytesResponse = this.s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build());
            GetObjectResponse getObjectResponse = bytesResponse.response();
            fileBucket.setContentLength(getObjectResponse.contentLength());
            fileBucket.setContentType(getObjectResponse.contentType());
            ByteArrayResource resource = new ByteArrayResource(bytesResponse.asByteArray());
            fileBucket.setResource(resource);
        } catch (SdkClientException e) {
            log.error(e.toString());
            log.error("downloadFile : AmazonServiceException (bucketName: {}, path: {} )", bucketName, path);
            throw new ServiceUnavailableException(ErrorCode.FAILED_DOWNLOAD_FROM_EXTERNAL);
        } catch (S3Exception e) {
            log.error(e.toString());
            log.error("downloadFile : S3Exception (bucketName: {}, path: {} )", bucketName, path);
            throw new ServiceUnavailableException(ErrorCode.FAILED_DOWNLOAD_FROM_EXTERNAL);
        }
        return fileBucket;
    }

    public void deleteFile(String bucketName, String path) throws NotFoundException {
        try {
            DeleteObjectResponse deleteObjectResponse = this.s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build());
            log.debug(deleteObjectResponse.toString());
            log.debug(deleteObjectResponse.requestChargedAsString());
        } catch (SdkClientException e) {
            log.error(e.toString());
            log.error("deleteFile : SdkClientException (bucketName: {}, path: {} )", bucketName, path);
            throw new ServiceUnavailableException(ErrorCode.FAILED_DELETE_FROM_EXTERNAL);
        } catch (S3Exception e) {
            log.error(e.toString());
            log.error("deleteFile : S3Exception (bucketName: {}, path: {} )", bucketName, path);
            throw new ServiceUnavailableException(ErrorCode.FAILED_DELETE_FROM_EXTERNAL);
        }
    }

    public String signBucket(String bucketName, String keyName, int time) {
        String myURL = null;

        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType("application/octet-stream")
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = this.s3Presigner.presignPutObject(presignRequest);

            myURL = presignedRequest.url().toString();
            log.debug("Presigned URL to upload a file to: " + myURL);
            log.debug("Which HTTP method needs to be used when uploading a file: " +
                    presignedRequest.httpRequest().method());

            // Upload content to the Amazon S3 bucket by using this URL
            // URL url = presignedRequest.url();

            // Create the connection and use it to upload the new object by using the presigned URL
            /*
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","image/png");
            connection.setRequestMethod("PUT");
            connection.getOutputStream().write(pic);
            connection.getResponseCode();
            System.out.println("HTTP response code is " + connection.getResponseCode());
            */
        } catch (S3Exception e) {
            e.getStackTrace();
        } /*catch (IOException e) {
            e.getStackTrace();
        }*/
        return myURL;
    }


    public String signedUrl(String bucketName, String objectKey, int mins) {
        String signedUrl = null;
        try {
            // Create a GetObjectRequest to be pre-signed
            GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build();

            // Create a GetObjectPresignRequest to specify the signature duration
            GetObjectPresignRequest getObjectPresignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(mins))
                            .getObjectRequest(getObjectRequest)
                            .build();

            // Generate the presigned request
            PresignedGetObjectRequest presignedGetObjectRequest =
                    this.s3Presigner.presignGetObject(getObjectPresignRequest);


            /*
            HttpURLConnection connection = (HttpURLConnection) presignedGetObjectRequest.url().openConnection();
            presignedGetObjectRequest.httpRequest().headers().forEach((header, values) -> {
                values.forEach(value -> {
                    connection.addRequestProperty(header, value);
                });
            });

            // Send any request payload that the service needs (not needed when isBrowserExecutable is true)
            if (presignedGetObjectRequest.signedPayload().isPresent()) {
                connection.setDoOutput(true);
                try (InputStream signedPayload = presignedGetObjectRequest.signedPayload().get().asInputStream();
                     OutputStream httpOutputStream = connection.getOutputStream()) {
                    IoUtils.copy(signedPayload, httpOutputStream);
                }
            }

            // Download the result of executing the request
            try (InputStream content = connection.getInputStream()) {
                System.out.println("Service returned response: ");
                IoUtils.copy(content, System.out);
            }*/
            // Log the presigned URL
            signedUrl = presignedGetObjectRequest.url().toString();
            log.debug("Presigned URL: " + presignedGetObjectRequest.url());
        } catch (SdkClientException e) {
            log.error(e.toString());
            log.error("signedUrl : SdkClientException (bucketName: {}, objectKey: {} )", bucketName, objectKey);
        } catch (S3Exception e) {
            log.error(e.toString());
            log.error("signedUrl : S3Exception (bucketName: {}, objectKey: {})", bucketName, objectKey);
        }
        /*catch (IOException e) {
            log.error("signedUrl : IOException (bucketName: {}, objectKey: {})", bucketName, objectKey);
        }*/

        return signedUrl;
    }


}

