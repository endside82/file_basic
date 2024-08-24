package com.endside.file.user.constants;

import lombok.Getter;

@Getter
public enum LoginType {

    EMAIL("EMAIL"), SOCIAL("SOCIAL");

    private final String loginType;

    LoginType(String loginType) {
        this.loginType = loginType.toUpperCase();
    }

    @Override
    public String toString() {
        return this.loginType;
    }

    public static LoginType getLoginTypeAsType(String loginType) {
        return LoginType.valueOf(loginType.toUpperCase());
    }
}