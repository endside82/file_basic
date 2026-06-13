package com.endside.file.manage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.error.exception.RestException;
import com.endside.file.manage.dto.FileBucket;
import com.endside.file.manage.response.FileUploadResponse;
import com.endside.file.manage.constant.FileConfig;
import com.endside.file.manage.param.BatchConfirmParam;
import com.endside.file.manage.service.BulkPresignedService;
import com.endside.file.manage.service.BulkUploadService;
import com.endside.file.manage.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file/mng/v1")
public class AdminFileUploadController {
    @Value("${admin.verification-key}")
    private String adminVerificationKey;

    private final FileUploadService fileUploadService;
    private final BulkUploadService bulkUploadService;
    private final BulkPresignedService bulkPresignedService;

    @CrossOrigin
    @PostMapping(value = "/{adminKey}/upload/{category}/{type}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFileAdmin(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "md5", required = false) String md5
    ) {
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

    @CrossOrigin
    @PostMapping(value = "/{adminKey}/upload/bulk/{category}/{type}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<byte[]> bulkUploadFromExcel(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestPart("file") MultipartFile excelFile
    ) {
        if (!adminKey.equals(adminVerificationKey)) {
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }

        if (excelFile.isEmpty()) {
            throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
        }

        String originalFileName = excelFile.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".xlsx")) {
            throw new InvalidParameterException(ErrorCode.INVALID_FILE_NAME);
        }

        byte[] resultExcel = bulkUploadService.bulkUploadFromExcel(excelFile, category, type);

        String resultFileName = "bulk_result_" + originalFileName;
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resultFileName + "\"")
                .contentLength(resultExcel.length)
                .body(resultExcel);
    }

    @CrossOrigin
    @PostMapping(value = "/{adminKey}/upload/confirm/batch/{category}")
    public ResponseEntity<byte[]> confirmBatchUpload(
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @RequestBody List<BatchConfirmParam> items
    ) {
        if (!adminKey.equals(adminVerificationKey)) {
            throw new RestException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }

        if (items == null || items.isEmpty()) {
            throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
        }

        if (items.size() > FileConfig.MAX_BATCH_SIZE) {
            throw new InvalidParameterException(ErrorCode.BATCH_SIZE_EXCEEDED);
        }

        byte[] resultExcel = bulkPresignedService.confirmAndExport(category, items);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bulk_confirm_result.xlsx\"")
                .contentLength(resultExcel.length)
                .body(resultExcel);
    }

    @CrossOrigin
    @RequestMapping(value = "/{adminKey}/download/{category}/{type}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> getFile(
            @RequestParam(name = "path") String path,
            @PathVariable("adminKey") String adminKey,
            @PathVariable("category") String category,
            @PathVariable("type") String type) {
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
