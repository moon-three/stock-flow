package com.seohee.online.event;

import java.util.Map;

public record OrderCanceledEvent(
        Long orderId, Map<Long, Long> productQuantityMap) {}
