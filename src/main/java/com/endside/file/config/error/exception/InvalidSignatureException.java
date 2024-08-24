package com.endside.file.config.error.exception;

import com.endside.file.config.error.ErrorCode;

public class InvalidSignatureException extends RestException {

    public InvalidSignatureException() {
        super();
    }

    public InvalidSignatureException(ErrorCode errorCode ){
        super(errorCode);
    }
}
