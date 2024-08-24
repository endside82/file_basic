package com.endside.file.user.constants;


import lombok.Getter;

@Getter
public enum Os {
    WEB(0), ANDROID(1), IOS(2);

    Os(int typeNum) {
        this.typeNum = typeNum;
    }

    private final int typeNum;

}
