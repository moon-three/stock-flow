package com.seohee.online.event;

import com.seohee.online.redis.dto.StockDecreaseMessage;
import com.seohee.online.redis.dto.StockRestoreMessage;
import com.seohee.online.redis.publisher.StockDecreasePublisher;
import com.seohee.online.redis.publisher.StockRestorePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderEventListener {

    private final StockDecreasePublisher stockDecreasePublisher;
    private final StockRestorePublisher stockRestorePublisher;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handle(OrderCreatedEvent event) {
        StockDecreaseMessage messageDto = new StockDecreaseMessage(
                event.orderId(), event.productQuantityMap());
        // pub으로 DB 재고 차감 + stockLog 생성
        stockDecreasePublisher.publishAsync(messageDto);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handle(OrderCanceledEvent event) {
        StockRestoreMessage messageDto = new StockRestoreMessage(
                event.orderId(), event.productQuantityMap());
        // pub으로 DB 재고 증가 + stockLog 생성
        stockRestorePublisher.publishAsync(messageDto);
    }
}
