package com.seohee.online.service.order;

import com.seohee.common.dto.OrderDto;

public interface OrderService {
    OrderDto.OrderDetailResponse placeOrder(OrderDto.OrderRequest orderRequest);

    OrderDto.OrderDetailResponse cancelOrder(Long orderId, Long userId);
}
