package com.seohee.online.service;

import com.seohee.common.dto.ProductDto;
import com.seohee.domain.entity.Product;
import com.seohee.online.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ProductDto.ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllByIsDeletedFalse();

        return products.stream()
                .map(product -> {
                    boolean isSoldOut = product.isSoldOut();

                    return new ProductDto.ProductResponse(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            isSoldOut
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ProductDto.ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않거나 삭제된 상품입니다."));
        if(product.isDeleted()) {
            throw new RuntimeException("존재하지 않거나 삭제된 상품입니다.");
        }
        boolean isSoldOut = product.isSoldOut();

        return new ProductDto.ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                isSoldOut
        );
    }
}
