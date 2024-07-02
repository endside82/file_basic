package com.endside.file.config.error.exception;

import com.endside.file.config.error.ErrorCode;

public class ServiceUnavailableException extends RestException {
    public ServiceUnavailableException() {
        super();
    }

    public ServiceUnavailableException(ErrorCode errorCode ){
        super(errorCode);
    }
}
