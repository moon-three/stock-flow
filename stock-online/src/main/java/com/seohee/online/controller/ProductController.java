package com.seohee.online.controller;

import com.seohee.common.dto.ProductDto;
import com.seohee.online.service.product.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductServiceImpl productServiceImpl;

    @GetMapping
    public ResponseEntity<List<ProductDto.ProductResponse>> getProducts() {
        List<ProductDto.ProductResponse> products = productServiceImpl.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto.ProductDetailResponse> getProductById(@PathVariable Long productId) {
        ProductDto.ProductDetailResponse product = productServiceImpl.getProductById(productId);
        return ResponseEntity.ok(product);
    }
}
