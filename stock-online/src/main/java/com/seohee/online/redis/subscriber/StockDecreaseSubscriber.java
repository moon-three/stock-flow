package com.seohee.online.redis.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seohee.online.redis.dto.StockDecreaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class StockDecreaseSubscriber {

    private final StockOrderService stockOrderService;

    private final ObjectMapper objectMapper;

    public void onMessage(String messageJson) {
        try {
            StockDecreaseMessage messageDto = objectMapper.readValue(messageJson, StockDecreaseMessage.class);
            try {
                stockOrderService.decreaseStockAndChangeOrderSuccess(messageDto);
            } catch (RuntimeException e) {
                stockOrderService.restoreStockAndChangeOrderFail(messageDto);
            }
        } catch (JsonProcessingException e) {
            log.error("[stockDecrease] JSON -> Map 변환 실패: {}", e.getMessage());
        }
    }
}
