package com.seohee.common.exception;

public class RedisOperationException extends BusinessException {
    public RedisOperationException() {
        super("시스템 오류가 발생했습니다.");
    }
}
