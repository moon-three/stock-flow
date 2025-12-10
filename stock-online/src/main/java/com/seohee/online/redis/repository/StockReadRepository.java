package com.seohee.online.redis.repository;

import java.util.Map;

public interface StockReadRepository {
    Map<Long, Long> findAllProducts();
    Map<Long, Long> findProductById(Long id);
}
