package com.seohee.online.redis.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seohee.online.redis.dto.StockRestoreMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRestorePublisher {

    private final ChannelTopic stockRestoreTopic;
    private final RedisPublisher redisPublisher;
    private final ExecutorService customExecutor;
    private final ObjectMapper objectMapper;

    public void publishAsync(StockRestoreMessage messageDto) {
        CompletableFuture.runAsync(() -> {
            try {
                String messageJson = objectMapper.writeValueAsString(messageDto);
                redisPublisher.publish(stockRestoreTopic, messageJson);
            } catch (JsonProcessingException e) {
                log.error("[StockRestore] Map -> JSON 변환 실패: {}", e.getMessage());
            }
        }, customExecutor);
    }
}
