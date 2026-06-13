package com.endside.file.user.constants;


import lombok.Getter;

import java.util.Arrays;

/**
 * 운영체제 Enum
 *
 * WEB(0), ANDROID(1), IOS(2), OTHER(3)
 */
@Getter
public enum Os {
    WEB(0),      // 웹
    ANDROID(1),  // 안드로이드
    IOS(2),      // iOS
    OTHER(3);    // 기타

    private final int typeNum;

    Os(int typeNum) {
        this.typeNum = typeNum;
    }

    // typeNum으로 Os 반환 (해당 값 없으면 OTHER)
    public static Os valueOfTypeNum(int typeNum) {
        return Arrays.stream(values())
                .filter(os -> os.typeNum == typeNum)
                .findFirst()
                .orElse(OTHER);
    }
}
