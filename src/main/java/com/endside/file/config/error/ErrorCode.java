package com.endside.file.config.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // AUTH common JWT api call (1000XX)
    JWT_TOKEN_AUTH_ERROR(401,100001,"JWT_TOKEN_AUTH_ERROR"),
    JWT_TOKEN_EXPIRATION(401,100002,"JWT_TOKEN_EXPIRATION"),
    INVALID_AUTH_TOKEN(401,100003,"INVALID_AUTH_TOKEN"),
    // USER LOGIN (102XX)
    LOGIN_FAILURE_USER_STATUS_EXIT(403,110205,"LOGIN_FAILURE_USER_STATUS_EXIT"),         // 탈퇴
    LOGIN_FAILURE_USER_STATUS_TRYEXIT(403,110206,"LOGIN_FAILURE_USER_STATUS_TRYEXIT"),   // 탈퇴진행
    LOGIN_FAILURE_USER_STATUS_LOGOUT(403,110207,"LOGIN_FAILURE_USER_STATUS_LOGOUT"),     // 로그아웃됨
    LOGIN_FAILURE_USER_STATUS_STOP(403,110208,"LOGIN_FAILURE_USER_STATUS_STOP"),         // 이용정지됨
    LOGIN_FAILURE_USER_STATUS_BAN(403,110209,"LOGIN_FAILURE_USER_STATUS_BAN"),           // 강제탈퇴
    // FILE (1308XX)
    INVALID_CATEGORY(400,130801,"INVALID_CATEGORY"),                             // Category 지정이 잘못 되어 있음
    INVALID_FILE_NAME(400,130802,"INVALID_FILE_NAME"),                           // 파일 이름이 없거나 올바르지 않음
    FILE_NOT_ATTACHED(400,130803,"FILE_NOT_ATTACHED"),                           // 파일 업로드시 파일이 첨부되어 있지 않음
    INVALID_REQUEST_FILE_PATH(400,130804,"INVALID_REQUEST_FILE_PATH"),           // 잘못된 파일 경로
    RESOURCE_NOT_PUBLIC(400,130805,"RESOURCE_NOT_PUBLIC"),                       // 공개된 경로가 아님 (not yet)
    FAILED_GET_INFO(404,130806,"FAILED_GET_INFO"),                               // 화이트리스트 정보 추출 실패 (not yet)
    // AWS S3(2002XX)
    FAILED_UPLOAD_TO_EXTERNAL(503,200201,"FAILED_UPLOAD_TO_EXTERNAL"),           // 외부 업로드 요청 실패
    FAILED_DOWNLOAD_FROM_EXTERNAL(503,200202,"FAILED_DOWNLOAD_FROM_EXTERNAL"),   // 외부 자원 요청 실패
    FAILED_DELETE_FROM_EXTERNAL(503,200203,"FAILED_DELETE_FROM_EXTERNAL"),       // 외부 삭제 요청 실패
    ;
    private final int code;
    private final int status;
    private final String message;

    ErrorCode(final int status, final int code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

}
