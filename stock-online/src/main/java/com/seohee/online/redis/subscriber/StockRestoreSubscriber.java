package com.seohee.online.redis.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seohee.online.redis.dto.StockRestoreMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class StockRestoreSubscriber {

    private final ObjectMapper objectMapper;
    private final StockOrderService stockOrderService;

    public void onMessage(String messageJson) {
        try {
            StockRestoreMessage messageDto = objectMapper.readValue(messageJson, StockRestoreMessage.class);
            try {
                stockOrderService.increaseStockAndChangeOrderCancel(messageDto);
            } catch (RuntimeException e) {
                stockOrderService.decreaseCacheStock(messageDto);
            }
        } catch (JsonProcessingException e) {
            log.error("[stockRestore] JSON -> Map 변환 실패: {}", e.getMessage());
        }
    }
}
