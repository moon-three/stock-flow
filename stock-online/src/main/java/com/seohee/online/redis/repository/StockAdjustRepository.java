package com.seohee.online.redis.repository;

import java.util.Map;

public interface StockAdjustRepository {
    boolean decreaseStock(Map<Long, Long> productMap);
    boolean restoreStock(Map<Long, Long> productMap);
}
