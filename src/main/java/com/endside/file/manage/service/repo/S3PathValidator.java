package com.endside.file.manage.service.repo;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.manage.constant.CategoryType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class S3PathValidator {

    private String profilePath = "\\w+/\\w+/.+";
    private String resourcePath = "\\w+/\\d{8}/.+";
    // private String groupPath = "\\w+/\\d{8}/.+";
    // private String adItemPath = "\\w+/\\w+/.+";
    // private String snsPath = "\\w+/\\w+/\\w+/.+";
    // private String aeItemPath = "\\d{8}/\\w+/.+";

    private Map<CategoryType, Pattern> patterns = new HashMap<>();

    public S3PathValidator() {
        patterns.put(CategoryType.PROFILE, Pattern.compile(profilePath));
        patterns.put(CategoryType.RESOURCE, Pattern.compile(resourcePath));
    }

    public boolean isValid(CategoryType categoryType, String path) {

        if (!StringUtils.hasText(path)) {
            throw new InvalidParameterException(ErrorCode.INVALID_REQUEST_FILE_PATH);
        }

        Pattern pattern = patterns.get(categoryType);

        if (Objects.isNull(pattern)) {
            throw new InvalidParameterException(ErrorCode.INVALID_CATEGORY);
        }

        return pattern.matcher(path).matches();
    }
}
