package com.endside.file.manage.constant;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class WhiteList {
    public WhiteList() {
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        try {
            whiteList = objectMapper.writeValueAsString(new WhiteListValue());
        } catch (JacksonException e) {
            throw new ServiceUnavailableException(ErrorCode.FAILED_GET_INFO);
        }
    }
    public static String whiteList;
}
