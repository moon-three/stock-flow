package com.seohee.online.redis.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seohee.common.exception.OrderNotExistException;
import com.seohee.common.exception.StockNotFoundException;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.Stock;
import com.seohee.domain.entity.StockLog;
import com.seohee.domain.enums.StockChangeType;
import com.seohee.online.redis.RedisService;
import com.seohee.online.redis.dto.StockDecreaseMessage;
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.StockLogRepository;
import com.seohee.online.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDecreaseSubscriber {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final StockLogRepository stockLogRepository;

    private final RedisService redisService;

    @Transactional
    public void onMessage(String messageJson) {
        try {
            StockDecreaseMessage messageDto = objectMapper.readValue(messageJson, StockDecreaseMessage.class);

            for(Map.Entry<Long, Long> entry : messageDto.productMap().entrySet()) {
                Long productId = entry.getKey();
                Long quantity = entry.getValue();

                Stock stock = stockRepository.findByProductId(productId)
                        .orElseThrow(() -> new StockNotFoundException());

                int updatedRows = stockRepository.decreaseQuantity(productId, quantity);
                if(updatedRows == 0) {
                    log.error("[stockDecrease] subscriber: 재고 차감 실패");
                    redisService.restoreStockInRedis(messageDto.productMap());

                    Order order = orderRepository.findById(messageDto.orderId())
                            .orElseThrow(() -> new OrderNotExistException());
                    order.changeOrderStatusToFail();

                    return;
                }

                StockLog stockLog = StockLog.from(stock, quantity, StockChangeType.ORDER);
                stockLogRepository.save(stockLog);
            }
            Order order = orderRepository.findById(messageDto.orderId())
                    .orElseThrow(() -> new OrderNotExistException());
            order.changeOrderStatusToSuccess();

        } catch (JsonProcessingException e) {
            log.error("[stockDecrease] JSON -> Map 변환 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
