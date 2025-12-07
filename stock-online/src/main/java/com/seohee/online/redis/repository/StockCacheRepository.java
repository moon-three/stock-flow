package com.seohee.online.redis.repository;

import java.util.Map;

public interface StockCacheRepository {
    boolean decreaseStock(Map<Long, Long> productMap);
    boolean restoreStock(Map<Long, Long> productMap);
}
