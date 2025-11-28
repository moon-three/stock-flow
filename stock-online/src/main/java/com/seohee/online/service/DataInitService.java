package com.seohee.online.service;

import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.Stock;
import com.seohee.domain.entity.User;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.StockRepository;
import com.seohee.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public void init() {
        log.info("데이터 초기화 시작");
        for(int i = 1; i <= 500; i++) {
            User user = User.builder()
                    .name("user" + i)
                    .email("user" + i + "@gmail.com")
                    .build();

            userRepository.save(user);
        }

        for(int i = 1; i <= 10; i++) {
            Product product = Product.builder()
                    .name("상품" + i)
                    .price(1000)
                    .description("상품" + i)
                    .build();

            productRepository.save(product);

            Stock stock = Stock.builder()
                    .product(product)
                    .quantity(500000)
                    .build();

            stockRepository.save(stock);
        }
    }

}
