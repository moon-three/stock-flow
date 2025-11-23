package com.seohee.online.service;

import com.seohee.common.dto.OrderDto;

public interface OrderService {
    OrderDto.OrderResponse placeOrder(OrderDto.OrderRequest orderRequest);
}
