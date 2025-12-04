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
import com.seohee.online.redis.dto.StockRestoreMessage;
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.StockLogRepository;
import com.seohee.online.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRestoreSubscriber {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final StockLogRepository stockLogRepository;
    private final RedisService redisService;

    @Transactional
    public void onMessage(String messageJson) {
        try {
            StockRestoreMessage messageDto = objectMapper.readValue(messageJson, StockRestoreMessage.class);

            for(Map.Entry<Long, Long> entry : messageDto.productMap().entrySet()) {
                Long productId = entry.getKey();
                Long quantity = entry.getValue();

                Stock stock =  stockRepository.findByProductId(productId)
                        .orElseThrow(() -> new StockNotFoundException());

                int updateRows = stockRepository.increaseQuantity(productId, quantity);
                if(updateRows == 0) {
                    log.error("[stockRestore] subscriber : 재고 복구 실패");
                    redisService.restoreStockInRedis(messageDto.productMap());

                    return;
                }

                StockLog stockLog = StockLog.from(stock, quantity, StockChangeType.CANCEL);
                stockLogRepository.save(stockLog);
            }
            Order order = orderRepository.findById(messageDto.orderId())
                    .orElseThrow(() -> new OrderNotExistException());
            order.changeOrderStatusToCancel();

        } catch (JsonProcessingException e) {
            log.error("[stockRestore] JSON -> Map 변환 실패: {}", e.getMessage());
        }

    }

}
