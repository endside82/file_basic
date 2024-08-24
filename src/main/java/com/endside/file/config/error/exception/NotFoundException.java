package com.endside.file.config.error.exception;

import com.endside.file.config.error.ErrorCode;

public class NotFoundException extends RestException {

    public NotFoundException() {
        super();
    }

    public NotFoundException(ErrorCode errorCode ){
        super(errorCode);
    }

}
