package com.seohee.online.service;

import com.seohee.common.dto.ProductDto;

import java.util.List;

public interface ProductService {
    List<ProductDto.ProductResponse> getAllProducts();
    ProductDto.ProductDetailResponse getProductById(Long id);
}
