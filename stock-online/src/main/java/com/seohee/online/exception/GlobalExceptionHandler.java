package com.seohee.online.exception;

import com.seohee.common.exception.BusinessException;
import com.seohee.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(BusinessException e) {
        int status = HttpStatus.BAD_REQUEST.value();
        return new ResponseEntity<>(
                    new ErrorResponse(status, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
    }
}
