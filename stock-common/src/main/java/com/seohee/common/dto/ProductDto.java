package com.seohee.common.dto;

public class ProductDto {

    public record ProductResponse(
            Long id, String name, long price, boolean isSoldOut
    ) {}

    public record ProductDetailResponse(
            Long id, String name, long price, String description, boolean isSoldOut
    ) {}
}
