package com.endside.file.manage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.error.exception.RestException;
import com.endside.file.manage.dto.FileBucket;
import com.endside.file.manage.response.FileUploadResponse;
import com.endside.file.manage.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file/mng/v1")
public class AdminFileUploadController {
    @Value("${admin.verification-key:admin}")
    private String adminVerificationKey;

    private final FileUploadService fileUploadService;

    /**
     * Admin 권한으로 파일 업로드
     */
    @CrossOrigin
    @PostMapping(value = "/{adminKey}/upload/{category}/{type}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFileAdmin(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "md5", required = false) String md5
    ) throws Exception {
        if(!adminKey.equals(adminVerificationKey)){
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }

        if (file.isEmpty()) {
            throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
        }

        String filePath = fileUploadService.uploadFile(file, category, type, "admin", md5);
        FileUploadResponse fileResponse = new FileUploadResponse();
        fileResponse.setPath(filePath);
        return ResponseEntity.status(HttpStatus.OK).body(fileResponse);
    }

    /**
     * Admin 권한으로 파일 다운로드
     */
    @CrossOrigin
    @RequestMapping(value = "/{adminKey}/download/{category}/{type}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> getFile(
            @RequestParam(name = "path") String path,
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type) throws Exception {
        if(!adminKey.equals(adminVerificationKey)){
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }
        FileBucket fileBucket = fileUploadService.getFile(category, path, type, "admin");
        String filename = path.substring(path.lastIndexOf("/")).replace("/", "");
        return ResponseEntity.status(HttpStatus.OK)
                .contentLength(fileBucket.getContentLength())
                .contentType(MediaType.parseMediaType(fileBucket.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileBucket.getResource());
    }

}
