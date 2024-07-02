package com.endside.file.config.security;

import com.endside.file.user.constants.UserStatus;
import com.endside.file.user.model.LoginAddInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;


@Getter
public class UserPrincipal implements UserDetails {

    @Builder
    public UserPrincipal(long userId, String email, String password, int status, String userHex, String mobile, Date birthDate, LoginAddInfo loginAddInfo, String nickname) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.status = status;
        this.userHex = userHex;
        this.mobile = mobile;
        this.birthDate = birthDate;
        this.loginAddInfo = loginAddInfo;
        this.nickname = nickname;
    }

    private final long userId;

    private final String email;

    private final String password;

    private final String userHex;

    private final int status;

    private final String mobile;

    private final Date birthDate;

    private final LoginAddInfo loginAddInfo;

    private final String nickname;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status == UserStatus.NORMAL.getStatus()
                || this.status == UserStatus.LOGOUT.getStatus();
    }

}

