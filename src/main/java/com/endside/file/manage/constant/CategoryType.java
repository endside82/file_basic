package com.endside.file.manage.constant;

import lombok.Getter;

public enum CategoryType {
    PROFILE("profile"),
    RESOURCE("resource"),
    LECTURE("lecture"),
    CHAPTER("chapter"),
    QUESTION("question"),
    QUIZ("quiz"),
    UNIVERSITY("university"),
    UNKNOWN("unknown");

    @Getter
    private String type;

    CategoryType(String type) {
        this.type = type;
    }

    public boolean equal(CategoryType other) {
        return (this.type.compareTo(other.type) == 0);
    }

    public static boolean equal(CategoryType one, CategoryType two) {
        return one.equal(two);
    }

    public static CategoryType getStorageTypeByCategory(String category) {
        return switch (category.toLowerCase()) {
            case "profile" -> PROFILE;
            case "resource" -> RESOURCE;
            case "lecture" -> LECTURE;
            case "chapter" -> CHAPTER;
            case "question" -> QUESTION;
            case "quiz" -> QUIZ;
            case "university" -> UNIVERSITY;
            default -> UNKNOWN;
        };
    }
}
