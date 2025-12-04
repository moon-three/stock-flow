package com.seohee.online.redis.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seohee.online.redis.RedisService;
import com.seohee.online.redis.dto.StockRestoreMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRestoreSubscriber {

    private final StockOrderService stockOrderService;

    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    public void onMessage(String messageJson) {
        try {
            StockRestoreMessage messageDto = objectMapper.readValue(messageJson, StockRestoreMessage.class);

            try {
                stockOrderService.increaseStockAndChangeOrderCancel(messageDto);
            } catch (RuntimeException e) {
                redisService.decreaseStockInRedis(messageDto.productMap());
            }

        } catch (JsonProcessingException e) {
            log.error("[stockRestore] JSON -> Map 변환 실패: {}", e.getMessage());
        }

    }

}
