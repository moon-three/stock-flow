package com.seohee.common.exception;

public class StockNotEnoughException extends BusinessException {
    public StockNotEnoughException() {
        super("재고가 부족합니다.");
    }
}
