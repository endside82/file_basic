package com.endside.file.manage.constant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class WhiteList {
    public WhiteList() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            whiteList = objectMapper.writeValueAsString(new WhiteListValue());
        } catch (JsonProcessingException e) {
            throw new ServiceUnavailableException(ErrorCode.FAILED_GET_INFO);
        }
    }
    public static String whiteList;
}
