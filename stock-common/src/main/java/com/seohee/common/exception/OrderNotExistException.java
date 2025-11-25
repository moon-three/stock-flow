package com.seohee.common.exception;

public class OrderNotExistException extends BusinessException {
    public OrderNotExistException() {
        super("해당 주문이 존재하지 않습니다.");
    }
}
