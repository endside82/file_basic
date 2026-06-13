package com.endside.file.manage.controller;

import com.endside.file.config.security.UserPrincipal;
import com.endside.file.manage.param.SignedParam;
import com.endside.file.manage.param.UploadSignedParam;
import com.endside.file.manage.response.SignedFileResponse;
import com.endside.file.manage.service.FileUploadService;
import com.endside.file.manage.constant.WhiteList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/file/mng/v1")
public class FileAccessController {

    private final FileUploadService fileUploadService;

    public FileAccessController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * @param userPrincipal
     * @param category
     * @param type
     * @param signedParam
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @PostMapping(value = "/get/signed/{category}/{type}")
    public ResponseEntity<?> accessImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody SignedParam signedParam) {
        String accountHex = userPrincipal.getUserHex();
        SignedFileResponse signedFileResponse = fileUploadService.getSignedPath(category, signedParam.getPath(), type, accountHex);
        return ResponseEntity.status(HttpStatus.OK)
                .body(signedFileResponse);
    }

    /**
     * @param category
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @PostMapping(value = "/upload/signed/{category}/{type}")
    public ResponseEntity<?> uploadPathImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("category") String category,
            @PathVariable("type") String type,
            @RequestBody UploadSignedParam uploadSignedParam) {
        String accountHex = userPrincipal.getUserHex();
        SignedFileResponse signedFileResponse = fileUploadService.getSignedUploadPath(category, type, accountHex, uploadSignedParam.getFormatName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(signedFileResponse);
    }

    @CrossOrigin
    @GetMapping(value = "/white/list")
    public ResponseEntity<?> whiteList() {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(WhiteList.whiteList);
    }

}
