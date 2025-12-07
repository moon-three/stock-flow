package com.seohee.online.service;

import com.seohee.common.dto.ProductDto;
import com.seohee.common.exception.ProductNotExistException;
import com.seohee.domain.entity.Product;
import com.seohee.online.redis.repository.ProductCacheRepository;
import com.seohee.online.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class RedisProductServiceImpl implements ProductService {

    private final ProductCacheRepository productCacheRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ProductDto.ProductResponse> getAllProducts() {
        Map<Long, Long> productMap = productCacheRepository.findAllProducts();
        List<Long> ids = new ArrayList<>(productMap.keySet());

        List<ProductDto.ProductResponse> productResponses = new ArrayList<>();
        for(Long id : ids) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotExistException());

            long quantity = productMap.get(id);
            boolean isSoldOut = quantity <= 0;

            productResponses.add(new ProductDto.ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    isSoldOut
            ));
        }

        return productResponses;
    }

    @Override
    public ProductDto.ProductDetailResponse getProductById(Long id) {
        Map<Long, Long> productMap = productCacheRepository.findProductById(id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotExistException());

        long quantity = productMap.get(id);
        boolean isSoldOut = quantity <= 0;

        return  new ProductDto.ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                isSoldOut
        );
    }
}
