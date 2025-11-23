package com.seohee.common.dto;

import java.util.List;

public class OrderDto {

    public record OrderRequest(
        Long userId,
        List<OrderProductRequest> orderProducts,
        Long totalAmount,
        DeliveryTypeRequest deliveryTypeRequest
    ) {}

    public record OrderProductRequest(
        Long productId, Long quantity, Long unitPrice
    ) {}

    public record OrderResponse(
        Long orderId,
        String orderStatus,
        String deliveryType,
        List<OrderProductInfo> orderProductInfos,
        Long totalAmount
    ) {}

    public record OrderProductInfo(
            String productName,
            Long quantity,
            Long unitPrice,
            Long subTotal
    ) {}
}
