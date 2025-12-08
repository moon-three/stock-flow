package com.seohee.online.service.product;

import com.seohee.common.dto.ProductDto;
import com.seohee.common.exception.ProductNotExistException;
import com.seohee.common.exception.StockNotFoundException;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.Stock;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ProductDto.ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllByIsDeletedFalse();

        return products.stream()
                .map(product -> {
                    Stock stock = stockRepository.findByProductId(product.getId())
                            .orElseThrow(() -> new StockNotFoundException());
                    boolean isSoldOut = stock.isSoldOut();

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
                .orElseThrow(() -> new ProductNotExistException());
        if(product.isDeleted()) {
            throw new ProductNotExistException();
        }

        Stock stock = stockRepository.findByProductId(product.getId())
                .orElseThrow(() -> new StockNotFoundException());
        boolean isSoldOut = stock.isSoldOut();

        return new ProductDto.ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                isSoldOut
        );
    }
}
