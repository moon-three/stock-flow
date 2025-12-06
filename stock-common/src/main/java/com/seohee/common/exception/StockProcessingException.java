package com.seohee.common.exception;

public class StockProcessingException extends BusinessException {
    public StockProcessingException() {
        super("재고 변경에 실패하였습니다.");
    }
}
