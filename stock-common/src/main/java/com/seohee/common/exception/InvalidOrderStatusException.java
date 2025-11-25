package com.seohee.common.exception;

public class InvalidOrderStatusException extends BusinessException {
    public InvalidOrderStatusException() {
        super("취소 가능한 주문 상태가 아닙니다.");
    }
}
