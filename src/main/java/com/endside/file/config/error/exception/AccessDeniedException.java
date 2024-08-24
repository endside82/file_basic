package com.endside.file.config.error.exception;

import com.endside.file.config.error.ErrorCode;

public class AccessDeniedException extends RestException {

    public AccessDeniedException() {
        super();
    }

    public AccessDeniedException(ErrorCode errorCode ){
        super(errorCode);
    }
}
