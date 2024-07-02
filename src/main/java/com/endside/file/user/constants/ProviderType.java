package com.endside.file.user.constants;

import lombok.Getter;

@Getter
public enum ProviderType {
    NAVER("NAVER"), // 소셜 로그인 - 네이버
    KAKAO("KAKAO"), // 소셜 로그인 - 카카오
    GOOGLE("GOOGLE"), // 소셜 로그인 - 구글
    // APPLE("APPLE") // 소셜 로그인 - 애플
    ;
    private final String provider;

    ProviderType(String provider) {
        this.provider = provider.toUpperCase();
    }

    @Override
    public String toString() {
        return this.provider;
    }

    public static ProviderType getProviderAsType(String provider) {
        return ProviderType.valueOf(provider.toUpperCase());
    }
}