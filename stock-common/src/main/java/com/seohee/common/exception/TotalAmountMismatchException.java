package com.seohee.common.exception;

public class TotalAmountMismatchException extends BusinessException {
    public TotalAmountMismatchException() {
        super("주문 총 합계금액이 일치하지 않습니다.");
    }
}
