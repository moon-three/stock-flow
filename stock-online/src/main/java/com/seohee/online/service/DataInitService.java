package com.seohee.online.service;

import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.Stock;
import com.seohee.domain.entity.User;
import com.seohee.online.redis.repository.RedisStockAdjustRepository;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.StockRepository;
import com.seohee.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    private final RedisStockAdjustRepository redisStockAdjustRepository;

    public void init() {
        log.info("데이터 초기화 시작");

        List<User> users = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        List<Stock> stocks = new ArrayList<>();

        for(int i = 1; i <= 500; i++) {
            users.add(User.builder()
                    .name("user" + i)
                    .email("user" + i + "@gmail.com")
                    .build());
        }
        userRepository.saveAll(users);

        for(int i = 1; i <= 10; i++) {
            products.add(Product.builder()
                    .name("상품" + i)
                    .price(1000L)
                    .description("상품" + i)
                    .build());
        }
        productRepository.saveAll(products);

        for(Product product : products) {
            stocks.add(Stock.builder()
                    .product(product)
                    .quantity(500000)
                    .build());
        }
        stockRepository.saveAll(stocks);

        for (Stock s : stocks) {
            redisStockAdjustRepository.setStock(s);
        }

        log.info("데이터 초기화 완료");
    }
}
