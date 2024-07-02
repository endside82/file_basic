package com.endside.file.manage.constant;

public class FileConfig {
    private FileConfig(){} // prevent instantiation
    public static final int IMAGE_SHARE_MIN = 10;
    public static final int VIDEO_SHARE_MIN = 60;
    public static final int FILE_SHARE_MIN = 60;
    public static final int DEFAULT_SHARE_MIN = 60;

    public static int getTimeByType(String type) {
        return switch (type) {
            case "image" -> IMAGE_SHARE_MIN;
            case "video" -> VIDEO_SHARE_MIN;
            case "file" -> FILE_SHARE_MIN;
            default -> DEFAULT_SHARE_MIN;
        };
    }
}
