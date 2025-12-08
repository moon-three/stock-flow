package com.seohee.online.controller;

import com.seohee.common.dto.OrderDto;
import com.seohee.online.service.order.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderServiceImpl orderServiceImpl;

    @PostMapping
    public ResponseEntity<OrderDto.OrderDetailResponse> placeOrder(
            @RequestBody OrderDto.OrderRequest orderRequest) {
        OrderDto.OrderDetailResponse response = orderServiceImpl.placeOrder(orderRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("{orderId}/users/{userId}/cancel")
    public ResponseEntity<OrderDto.OrderDetailResponse> cancelOrder(
            @PathVariable Long orderId, @PathVariable Long userId) {
        OrderDto.OrderDetailResponse response = orderServiceImpl.cancelOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }

}
