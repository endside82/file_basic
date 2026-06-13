package com.endside.file.manage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.error.exception.RestException;
import com.endside.file.manage.constant.FileConfig;
import com.endside.file.manage.param.BatchUploadSignedParam;
import com.endside.file.manage.param.SignedParam;
import com.endside.file.manage.param.UploadSignedParam;
import com.endside.file.manage.response.BatchSignedFileResponse;
import com.endside.file.manage.response.SignedFileResponse;
import com.endside.file.manage.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file/mng/v1")
public class AdminFileAccessController {
    @Value("${admin.verification-key}")
    private String adminVerificationKey;

    private final FileUploadService fileUploadService;

    /**
     * @param category
     * @param type
     * @param signedParam
     * @returnØ
     * @throws Exception
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
        SignedFileResponse signedFileResponse = fileUploadService.getSignedPath(category, signedParam.getPath(), type, "admin");
        return ResponseEntity.status(HttpStatus.OK)
                .body(signedFileResponse);
    }

    @CrossOrigin
    @PostMapping(value = "/{adminKey}/upload/signed/batch/{category}/{type}")
    public ResponseEntity<?> batchUploadSigned(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody BatchUploadSignedParam batchParam) {
        if (!adminKey.equals(adminVerificationKey)) {
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }

        if (batchParam.getFiles() == null || batchParam.getFiles().isEmpty()) {
            throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
        }

        if (batchParam.getFiles().size() > FileConfig.MAX_BATCH_SIZE) {
            throw new InvalidParameterException(ErrorCode.BATCH_SIZE_EXCEEDED);
        }

        List<SignedFileResponse> items = batchParam.getFiles().stream()
                .map(param -> {
                    SignedFileResponse resp = fileUploadService.getSignedUploadPath(category, type, "admin", param.getFormatName());
                    resp.setOriginName(param.getFormatName());
                    return resp;
                })
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(BatchSignedFileResponse.builder().items(items).build());
    }

    /**
     * @param category
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @PostMapping(value = "/{adminKey}/upload/signed/{category}/{type}")
    public ResponseEntity<?> uploadPathImage(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody UploadSignedParam uploadSignedParam) {
        if(!adminKey.equals(adminVerificationKey)){
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }
        SignedFileResponse signedFileResponse = fileUploadService.getSignedUploadPath(category, type, "admin", uploadSignedParam.getFormatName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(signedFileResponse);
    }

}
