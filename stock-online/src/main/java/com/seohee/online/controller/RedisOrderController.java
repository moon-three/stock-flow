package com.seohee.online.controller;

import com.seohee.common.dto.OrderDto;
import com.seohee.online.service.RedisOrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Profile("!test")
@RequiredArgsConstructor
@RequestMapping("/api/redis/orders")
public class RedisOrderController {

    private final RedisOrderServiceImpl redisOrderServiceImpl;

    @PostMapping
    public ResponseEntity<OrderDto.OrderDetailResponse> placeOrderWithRedis(
            @RequestBody OrderDto.OrderRequest orderRequest) {
        OrderDto.OrderDetailResponse response = redisOrderServiceImpl.placeOrder(orderRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("{orderId}/users/{userId}/cancel")
    public ResponseEntity<OrderDto.OrderDetailResponse> cancelOrderWithRedis(
            @PathVariable Long orderId, @PathVariable Long userId) {
        OrderDto.OrderDetailResponse response = redisOrderServiceImpl.cancelOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }
}
