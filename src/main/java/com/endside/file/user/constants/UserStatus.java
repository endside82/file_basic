package com.endside.file.user.constants;

import lombok.Getter;

@Getter
public enum UserStatus {
    NORMAL(0),
    LOGOUT(1),
    STOP(2),
    BAN(3),
    TRYEXIT(4),
    EXIT(5);
    final int status;

    UserStatus(int status){
        this.status = status;
    }

}
