package com.seohee.online.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class StockDecreasePublisher {

    private final ChannelTopic stockDecreaseTopic;
    private final RedisPublisher redisPublisher;
    private final ExecutorService customExecutor;

    public void publishAsync(Map<Long, Long> productMap) {
        CompletableFuture.runAsync(() -> {
            redisPublisher.publish(stockDecreaseTopic, productMap);
        }, customExecutor);
    }
}
