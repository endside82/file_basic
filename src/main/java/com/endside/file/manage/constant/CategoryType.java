package com.endside.file.manage.constant;

import lombok.Getter;

public enum CategoryType {
    PROFILE("profile"),
    RESOURCE("resource"),
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
        switch (category.toLowerCase()) {
            case "profile": return PROFILE;
            case "resource": return RESOURCE;
            default: return UNKNOWN;
        }
    }
}
