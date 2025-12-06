package com.seohee.online.redis;

import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface StockCacheRepository {
    boolean decreaseStock(Map<Long, Long> productMap);
    boolean restoreStock(Map<Long, Long> productMap);
}
