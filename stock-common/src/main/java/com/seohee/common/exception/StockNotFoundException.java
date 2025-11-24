package com.seohee.common.exception;

public class StockNotFoundException extends BusinessException {
    public StockNotFoundException() {
        super("재고를 찾을 수 없습니다.");
    }
}
