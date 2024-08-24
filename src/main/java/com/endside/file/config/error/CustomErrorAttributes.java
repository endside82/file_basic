package com.endside.file.config.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {


    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

        // format & update timestamp
        Object timestamp = errorAttributes.get("timestamp");
        if (timestamp == null) {
            errorAttributes.put("timestamp", ResponseConstants.DATE_FORMAT.format(new Date()));
        } else {
            errorAttributes.put("timestamp", ResponseConstants.DATE_FORMAT.format((Date) timestamp));
        }

        int error_code = 99000;
        Object statusObj = errorAttributes.get("status");

        if(statusObj != null) {
            int status = (Integer)statusObj;
            error_code = error_code + status;
        }

        errorAttributes.put("error_code" , error_code);

        errorAttributes.remove("error");
        errorAttributes.remove("status");
        errorAttributes.remove("path");
        errorAttributes.remove("message");

        return errorAttributes;
    }
}