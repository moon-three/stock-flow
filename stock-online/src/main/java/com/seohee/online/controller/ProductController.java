package com.seohee.online.controller;

import com.seohee.common.dto.ProductDto;
import com.seohee.online.service.ProductService;
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

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto.ProductResponse>> getProducts() {
        List<ProductDto.ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ProductDto.ProductDetailResponse> getProductById(@PathVariable Long itemId) {
        ProductDto.ProductDetailResponse product = productService.getProductById(itemId);
        return ResponseEntity.ok(product);
    }
}
