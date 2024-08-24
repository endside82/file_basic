package com.endside.file.config.error.exception;

import com.endside.file.config.error.ErrorCode;

public class InvalidParameterException extends RestException {

    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(ErrorCode errorCode ){
        super(errorCode);
    }
}
