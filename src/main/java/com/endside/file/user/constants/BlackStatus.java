package com.endside.file.user.constants;

import lombok.Getter;

@Getter
public enum BlackStatus {
    // 탈퇴한 사용자
    EXIT(10205,"exit"), // status:1
    // 탈퇴진행 사용자
    TRYEXIT(10206,"tryexit"), // status:2
    // 로구아웃한 사용자
    LOGOUT(10207,"logout"), // status:3
    // 이용정지
    STOP(	10208,"stop"), // status:4
    // 강제 탈퇴 사용자
    BAN(10209,"ban"), // status:5
    ;

    private final int error_code;
    private final String status;

    BlackStatus(final int error_code, final String status) {
        this.error_code = error_code;
        this.status = status;
    }

}
