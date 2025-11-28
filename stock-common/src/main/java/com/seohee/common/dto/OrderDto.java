package com.seohee.common.dto;

import java.util.List;

public class OrderDto {

    public record OrderRequest(
        Long userId,
        List<OrderProductRequest> orderProducts,
        long totalAmount,
        DeliveryTypeRequest deliveryTypeRequest
    ) {}

    public record OrderProductRequest(
        Long productId,
        long quantity,
        long unitPrice
    ) {}

    public record OrderDetailResponse(
        Long orderId,
        String orderStatus,
        String deliveryType,
        List<OrderProductInfo> orderProductInfos,
        long totalAmount
    ) {}

    public record OrderProductInfo(
        Long productId,
        String productName,
        long quantity,
        long unitPrice,
        long subTotal
    ) {}
}
