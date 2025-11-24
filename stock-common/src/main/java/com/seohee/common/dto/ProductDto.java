package com.seohee.common.dto;

public class ProductDto {

    public record ProductResponse(
            Long id, String name, Long price, boolean isSoldOut
    ) {}

    public record ProductDetailResponse(
            Long id, String name, Long price, String description, boolean isSoldOut
    ) {}
}
