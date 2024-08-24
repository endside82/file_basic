package com.endside.file.config.error;

import com.endside.file.config.error.exception.NotFoundException;
import com.endside.file.config.error.exception.RestException;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final int STANDARD_SPRING_MVC_EXCEPTION_BASE_CODE = 11000;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> customHandleNotFound(RestException ex, WebRequest request) {
        printRequestLog(request);
        return exceptionResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<?> commonErrorHandler(RestException ex, WebRequest request){
        printRequestLog(request);
        return exceptionResponse(ex,HttpStatus.valueOf(ex.getErrorCode().getStatus()));
    }
    protected ResponseEntity<?> exceptionResponse(RestException ex , HttpStatus statusCode ){
        log.error("exceptionResponse", ex);
        String message = StringUtil.isNullOrEmpty(ex.getMessage()) ? "UNKNOWN_ERROR" : ex.getMessage();
        return new ResponseEntity<>(CustomErrorResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .errorCode(ex.getErrorCode().getCode()).build(), statusCode);
    }

    protected void printRequestLog(WebRequest request){
        try {
            HttpServletRequest nativeRequest = (HttpServletRequest) ((ServletWebRequest) request).getNativeRequest();
            InputStream inputStream = nativeRequest.getInputStream();
            byte[] body = StreamUtils.copyToByteArray(inputStream);
            log.error("Error param body is: " + new String(body));
        } catch (ClassCastException | IOException e) {
            log.error("fail to log");
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> commonExceptionHandler(Exception exception){
        return exceptionResponse(exception);
    }

    protected ResponseEntity<?> exceptionResponse(Exception exception){
        log.error("exceptionResponse", exception);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(CustomErrorResponse.builder()
                .message(status.getReasonPhrase())
                .timestamp(LocalDateTime.now())
                .errorCode(STANDARD_SPRING_MVC_EXCEPTION_BASE_CODE + status.value()).build(), status);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode code, WebRequest request) {
        // TODO check HttpStatus -> HttpStatusCode
        printRequestLog(request);
        return (ResponseEntity<Object>) exceptionResponse(exception, code);
    }

    protected ResponseEntity<?> exceptionResponse(Exception exception, HttpStatusCode code) {
        log.error("exceptionResponse", exception);
        return new ResponseEntity<>(CustomErrorResponse.builder()
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .errorCode(STANDARD_SPRING_MVC_EXCEPTION_BASE_CODE + code.value()).build(), code);
    }
}
