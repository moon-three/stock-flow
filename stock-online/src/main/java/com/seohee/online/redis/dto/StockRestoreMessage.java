package com.seohee.online.redis.dto;

import java.util.Map;

public record StockRestoreMessage(
        Long orderId, Map<Long, Long> productMap) {}
