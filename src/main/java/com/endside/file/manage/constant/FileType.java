package com.endside.file.manage.constant;

import lombok.Getter;

public enum FileType {
    IMAGE(1, "image"),
    VIDEO(2, "video"),
    NONE(3, "none"),
    STATIC(4, "static"),
    AUDIO(5, "audio"),
    UNKNOWN(0, "unknown");

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
        return switch (typePath.toLowerCase()) {
            case "image" -> IMAGE;
            case "video" -> VIDEO;
            case "none" -> NONE;
            case "static" -> STATIC;
            case "audio" -> AUDIO;
            default -> UNKNOWN;
        };
    }
}
