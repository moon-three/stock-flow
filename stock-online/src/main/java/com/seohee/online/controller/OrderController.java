package com.seohee.online.controller;

import com.seohee.common.dto.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<OrderDto.OrderResponse> placeOrder(
            @RequestBody OrderDto.OrderRequest orderRequest) {

        return ResponseEntity.ok(null);
    }
}
