package com.seohee.common.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("회원 정보를 찾을 수 없습니다.");
    }
}
