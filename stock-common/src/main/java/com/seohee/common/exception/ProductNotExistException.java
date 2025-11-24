package com.seohee.common.exception;

public class ProductNotExistException extends BusinessException {
    public ProductNotExistException() {
        super("존재하지 않거나 삭제된 상품입니다.");
    }
}
