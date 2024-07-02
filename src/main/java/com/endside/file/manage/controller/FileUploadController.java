package com.endside.file.manage.controller;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.security.UserPrincipal;
import com.endside.file.manage.dto.FileBucket;
import com.endside.file.manage.param.DeleteFileParam;
import com.endside.file.manage.param.DownloadFileParam;
import com.endside.file.manage.service.FileUploadService;
import com.endside.file.manage.response.FileDeleteResponse;
import com.endside.file.manage.response.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/file/mng/v1")
public class FileUploadController {

    FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * 카테고리 별 파일 업로드
     * @param category 카테고리
     * @param type 파일타입  image / video / none
     * @param file 파일
     * @return 파일 패스
     * @throws Exception
     */
    @CrossOrigin
    @PostMapping(value = "/upload/{category}/{type}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFile(
            Authentication authentication,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "md5" , required = false) String md5
    ) throws Exception {
        if (file.isEmpty()) {
            throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
        }
        String accountHex = ((UserPrincipal)authentication.getPrincipal()).getUserHex();
        String filePath = fileUploadService.uploadFile(file, category, type,  accountHex, md5);
        FileUploadResponse fileResponse = new FileUploadResponse();
        fileResponse.setPath(filePath);
        return ResponseEntity.status(HttpStatus.OK).body(fileResponse);
    }


    /**
     * 멀티 파일 업로드
     * @return
     * @throws Exception 예외
     */
    @CrossOrigin
    @PostMapping(value = "/upload/{category}/{type}/multi", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFiles(
            Authentication authentication,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestPart MultipartFile[] files) throws Exception {
        if (files == null || files.length == 0) {
            throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
        }
        String accountHex = ((UserPrincipal)authentication.getPrincipal()).getUserHex();
        List<FileUploadResponse> fileMultiUploadResponses = fileUploadService.uploadFiles(files, category, type, accountHex);
        return ResponseEntity.status(HttpStatus.OK).body(fileMultiUploadResponses);
    }

    /**
     * 이미지 다운로드
     * @param category 카테고리
     * @param downloadFileParam 이미지 경로 (s3 키 값에 대응)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/download/{category}/{type}", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<?> getFile(
            Authentication authentication,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody DownloadFileParam downloadFileParam) throws Exception {
        String accountHex = ((UserPrincipal)authentication.getPrincipal()).getUserHex();
        String path = downloadFileParam.getPath();
        FileBucket fileBucket = fileUploadService.getFile(category, path, type, accountHex);
        String filename = path.substring(path.lastIndexOf("/")).replace("/", "");
        return ResponseEntity.status(HttpStatus.OK)
                .contentLength(fileBucket.getContentLength())
                .contentType(MediaType.parseMediaType(fileBucket.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileBucket.getResource());
    }


    /**
     * 퍼블릭 이미지 다운로드
     * @param path 이미지 경로 (s3 키 값에 대응)
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/download/public/resource")
    public ResponseEntity<?> getPublicFile( @RequestParam(name = "path") String path) throws Exception {
        FileBucket fileBucket = fileUploadService.getPublicFile(path);
        String filename = path.substring(path.lastIndexOf("/")).replace("/", "");
        return ResponseEntity.status(HttpStatus.OK)
                .contentLength(fileBucket.getContentLength())
                .contentType(MediaType.parseMediaType(fileBucket.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileBucket.getResource());
    }

    /**
     * 이미지 삭제
     * @param authentication
     * @param category 카테고리
     * @param type 파일타입  image / video / none
     * @param deleteFileReqDto 이미지 삭제 정보
     * @return
     * @throws Exception
     */
    @DeleteMapping(value = "/delete/{category}/{type}")
    public ResponseEntity<?> deleteFile(
            Authentication authentication,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody DeleteFileParam deleteFileReqDto) throws Exception {
        String accountHex = ((UserPrincipal)authentication.getPrincipal()).getUserHex();
        fileUploadService.deleteFile(category, deleteFileReqDto.getPath());
        FileDeleteResponse fileDeleteResponse = new FileDeleteResponse(deleteFileReqDto.getPath());
        return ResponseEntity.status(HttpStatus.OK).body(fileDeleteResponse);
    }

}
