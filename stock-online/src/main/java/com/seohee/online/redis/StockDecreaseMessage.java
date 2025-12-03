package com.seohee.online.redis;

import java.util.Map;

public record StockDecreaseMessage(
        Long orderId, Map<Long, Long> productMap) {
}
