package com.endside.file.config.security;

import com.endside.file.user.model.LoginAddInfo;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * 추가 필드를 받기 위한 커스텀 인증 토큰 (자격 증명)
 */
@Getter
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final LoginAddInfo loginAddInfo;

    public CustomAuthenticationToken(Object principal, Object credentials, LoginAddInfo loginAddInfo) {
        super(principal, credentials);
        this.loginAddInfo = loginAddInfo;
        super.setAuthenticated(false);
    }

    /*
    public CustomAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, LoginAddInfo loginAddInfo) {
        super(principal, credentials, authorities);
        this.loginAddInfo = loginAddInfo;
        super.setAuthenticated(false);
    }
    */
}
