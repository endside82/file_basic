package com.endside.file.manage.constant;

import lombok.Getter;

public enum FileType {
    IMAGE(1,"image"),VIDEO(2,"video"),NONE(3,"none"),UNKNOWN(0,"unknown");

    private int type;
    @Getter
    private String typeString;

    FileType(int type, String typeString) {
        this.type = type;
        this.typeString = typeString;
    }

    public boolean equal(FileType other) {
        return (this.type == other.type);
    }

    public static boolean equal(FileType one, FileType two) {
        return one.equal(two);
    }

    public static FileType getFileTypeByTypePath(String typePath) {
        switch (typePath.toLowerCase()) {
            case "image": return IMAGE;
            case "video": return VIDEO;
            case "none": return NONE;
            default: return UNKNOWN;
        }
    }
}
