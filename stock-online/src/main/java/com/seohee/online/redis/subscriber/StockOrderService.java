package com.seohee.online.redis.subscriber;

import com.seohee.common.exception.OrderNotExistException;
import com.seohee.common.exception.StockNotFoundException;
import com.seohee.common.exception.StockProcessingException;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.Stock;
import com.seohee.domain.entity.StockLog;
import com.seohee.domain.enums.StockChangeType;
import com.seohee.online.redis.RedisService;
import com.seohee.online.redis.dto.StockDecreaseMessage;
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
public class StockOrderService {

    private final RedisService redisService;

    private final StockRepository stockRepository;
    private final StockLogRepository stockLogRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void decreaseStockAndChangeOrderSuccess(StockDecreaseMessage messageDto) {
        for(Map.Entry<Long, Long> entry : messageDto.productMap().entrySet()) {
            Long productId = entry.getKey();
            Long quantity = entry.getValue();

            Stock stock = stockRepository.findByProductId(productId)
                    .orElseThrow(() -> new StockNotFoundException());

            int updatedRows = stockRepository.decreaseQuantity(productId, quantity);
            if(updatedRows == 0) {
                log.error("DB 재고 차감 실패");
                throw new StockProcessingException();
            }

            StockLog stockLog = StockLog.from(stock, quantity, StockChangeType.ORDER);
            stockLogRepository.save(stockLog);
        }
        Order order = orderRepository.findById(messageDto.orderId())
                .orElseThrow(() -> new OrderNotExistException());
        order.changeOrderStatusToSuccess();
    }

    // redis 복구 & OrderStatus= Fail
    public void restoreStockAndChangeOrderFail(StockDecreaseMessage messageDto) {
        Order order = orderRepository.findById(messageDto.orderId())
                .orElseThrow(() -> new OrderNotExistException());
        order.changeOrderStatusToFail();
        redisService.restoreStockInRedis(messageDto.productMap());
    }

    @Transactional
    public void increaseStockAndChangeOrderCancel(StockRestoreMessage messageDto) {
        for(Map.Entry<Long, Long> entry : messageDto.productMap().entrySet()) {
            Long productId = entry.getKey();
            Long quantity = entry.getValue();

            Stock stock =  stockRepository.findByProductId(productId)
                    .orElseThrow(() -> new StockNotFoundException());

            int updateRows = stockRepository.increaseQuantity(productId, quantity);
            if(updateRows == 0) {
                log.error("DB 재고 복구 실패");
                throw new StockProcessingException();
            }

            StockLog stockLog = StockLog.from(stock, quantity, StockChangeType.CANCEL);
            stockLogRepository.save(stockLog);
        }
        Order order = orderRepository.findById(messageDto.orderId())
                .orElseThrow(() -> new OrderNotExistException());
        order.changeOrderStatusToCancel();
    }
}
