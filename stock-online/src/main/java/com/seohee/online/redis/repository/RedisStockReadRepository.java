package com.seohee.online.redis.repository;

import com.seohee.common.exception.RedisOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class RedisStockReadRepository implements StockReadRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final String STOCK_KEY_PREFIX = "stock:";

    @Override
    public Map<Long, Long> findAllProducts() {
        List<String> keys = new ArrayList<>(redisTemplate.keys(STOCK_KEY_PREFIX + "*"));
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        if(values == null) {
            throw new RedisOperationException();
        }

        Map<Long, Long> productQuantityMap = new HashMap<>();
        // Service에서 바로 데이터 사용할 수 있도록 하기 위해서
        // Redis key의 prefix 제거 후 Long으로 변환
        for(int i = 0; i < keys.size(); i++) {
            String value = values.get(i);
            if(value != null) {
                long productId = Long.parseLong(keys.get(i).substring(STOCK_KEY_PREFIX.length()));
                long quantity = Long.parseLong(values.get(i));
                productQuantityMap.put(productId, quantity);
            }
        }

        return productQuantityMap;
    }

    @Override
    public Map<Long, Long> findProductById(Long id) {
        String key =  STOCK_KEY_PREFIX + id;
        String value = redisTemplate.opsForValue().get(key);
        if(value == null) {
            throw new RedisOperationException();
        }

        Map<Long, Long> productQuantityMap = new HashMap<>();

        long productId = Long.parseLong(key.substring(STOCK_KEY_PREFIX.length()));
        long quantity = Long.parseLong(value);
        productQuantityMap.put(productId, quantity);

        return productQuantityMap;
    }
}
