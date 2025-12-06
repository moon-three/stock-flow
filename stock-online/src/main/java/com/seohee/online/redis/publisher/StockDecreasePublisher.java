package com.seohee.online.redis.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seohee.online.redis.dto.StockDecreaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class StockDecreasePublisher {

    private final ChannelTopic stockDecreaseTopic;
    private final RedisPublisher redisPublisher;
    private final ExecutorService customExecutor;
    private final ObjectMapper objectMapper;

    public void publishAsync(StockDecreaseMessage messageDto) {
        CompletableFuture.runAsync(() -> {
            try {
                String messageJson = objectMapper.writeValueAsString(messageDto);
                redisPublisher.publish(stockDecreaseTopic, messageJson);
            } catch (JsonProcessingException e) {
                log.error("[StockDecrease] Map -> JSON 변환 실패: {}", e.getMessage());
            }
        }, customExecutor);
    }
}
