package com.endside.file.manage.controller;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.RestException;
import com.endside.file.manage.param.SignedParam;
import com.endside.file.manage.param.UploadSignedParam;
import com.endside.file.manage.response.SignedFileResponse;
import com.endside.file.manage.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/file/mng/v1")
public class AdminFileAccessController {
    @Value("${admin.verification-key:admin}")
    private String adminVerificationKey;

    private final FileUploadService fileUploadService;

    public AdminFileAccessController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Admin 권한으로 Signed 다운로드 URL 생성
     */
    @CrossOrigin
    @PostMapping(value = "/{adminKey}/get/signed/{category}/{type}")
    public ResponseEntity<?> accessImage(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody SignedParam signedParam) {
        if(!adminKey.equals(adminVerificationKey)){
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }
        String url = fileUploadService.getSignedPath(category, signedParam.getPath(), type, "admin");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SignedFileResponse(url));
    }

    /**
     * Admin 권한으로 Signed 업로드 URL 생성
     */
    @CrossOrigin
    @PostMapping(value = "/{adminKey}/upload/signed/{category}/{type}")
    public ResponseEntity<?> uploadPathImage(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody UploadSignedParam uploadSignedParam) throws Exception {
        if(!adminKey.equals(adminVerificationKey)){
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }
        String url = fileUploadService.getSignedUploadPath(category, type, "admin", uploadSignedParam.getFormatName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SignedFileResponse(url));
    }

}
