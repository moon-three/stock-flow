package com.seohee.online.service;

import com.seohee.common.dto.OrderDto;

public interface OrderService {
    OrderDto.OrderDetailResponse placeOrder(OrderDto.OrderRequest orderRequest);

    OrderDto.OrderDetailResponse cancelOrder(Long orderId, Long userId);
}
