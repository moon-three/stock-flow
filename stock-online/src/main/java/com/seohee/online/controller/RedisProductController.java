package com.seohee.online.controller;

import com.seohee.common.dto.ProductDto;
import com.seohee.online.service.product.RedisProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile("!test")
@RequiredArgsConstructor
@RequestMapping("/api/redis/products")
public class RedisProductController {

    private final RedisProductServiceImpl redisProductServiceImpl;

    @GetMapping
    public ResponseEntity<List<ProductDto.ProductResponse>> getProducts() {
        List<ProductDto.ProductResponse> products = redisProductServiceImpl.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto.ProductDetailResponse> getProductById(@PathVariable Long productId) {
        ProductDto.ProductDetailResponse product = redisProductServiceImpl.getProductById(productId);
        return ResponseEntity.ok(product);
    }
}
