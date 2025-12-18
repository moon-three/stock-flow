package com.seohee.online.event;

import java.util.Map;

public record OrderCreatedEvent(
        Long orderId, Map<Long, Long> productQuantityMap) {}
