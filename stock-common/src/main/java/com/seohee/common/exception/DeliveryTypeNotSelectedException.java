package com.seohee.common.exception;

public class DeliveryTypeNotSelectedException extends BusinessException {
    public DeliveryTypeNotSelectedException() {
        super("배송 타입이 선택되지 않았습니다.");
    }
}
