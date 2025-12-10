package com.seohee.online.service.product;

import com.seohee.common.dto.ProductDto;
import com.seohee.common.exception.ProductNotExistException;
import com.seohee.domain.entity.Product;
import com.seohee.online.redis.repository.StockReadRepository;
import com.seohee.online.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class RedisProductServiceImpl implements ProductService {

    private final StockReadRepository stockReadRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ProductDto.ProductResponse> getAllProducts() {
        Map<Long, Long> productQuantityMap = stockReadRepository.findAllProducts();
        List<Long> ids = new ArrayList<>(productQuantityMap.keySet());

        List<ProductDto.ProductResponse> productResponses = new ArrayList<>();
        for(Long id : ids) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotExistException());

            long quantity = productQuantityMap.get(id);
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

    @Transactional(readOnly = true)
    @Override
    public ProductDto.ProductDetailResponse getProductById(Long id) {
        Map<Long, Long> productQuantityMap = stockReadRepository.findProductById(id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotExistException());

        long quantity = productQuantityMap.get(id);
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
