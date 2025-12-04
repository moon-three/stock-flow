package com.seohee.common.exception;

public class UserMismatchException extends BusinessException {
    public UserMismatchException() {
        super("접근 권한이 없습니다.");
    }
}
