package com.endside.file.manage.service;

import lombok.Getter;

public enum DataLifeCycle {
    SHORTTERM("fffa-", 30),
    MIDTERM("fffb-", 90),
    LONGTREM("fffc-", 180),
    ETERNAL("fffd-", 1095),
    ;

    @Getter
    private String head;
    @Getter
    private int days;
    DataLifeCycle(String head, int days) {
        this.head = head;
        this.days = days;
    }

}
