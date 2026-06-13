package com.endside.file.user.constants;

import lombok.Getter;

/**
 * 로그인 타입 Enum
 *
 * EMAIL(0), MOBILE(1), SOCIAL(2), ID_PASS(3), GUEST(4)
 */
@Getter
public enum LoginType {
    EMAIL(0, "EMAIL"),      // 이메일 로그인
    MOBILE(1, "MOBILE"),    // 휴대폰 번호 로그인
    SOCIAL(2, "SOCIAL"),    // 소셜 로그인 (카카오, 네이버, 구글 등)
    ID_PASS(3, "ID_PASS"),  // ID/비밀번호 로그인
    GUEST(4, "GUEST");      // 게스트 (비회원) 로그인

    private final String loginType;
    private final int typeNum;

    LoginType(int typeNum, String loginType) {
        this.typeNum = typeNum;
        this.loginType = loginType.toUpperCase();
    }

    @Override
    public String toString() {
        return this.loginType;
    }

    public static LoginType getLoginTypeAsType(String loginType) {
        if (loginType == null) {
            return null;
        }
        return LoginType.valueOf(loginType.toUpperCase());
    }
}
