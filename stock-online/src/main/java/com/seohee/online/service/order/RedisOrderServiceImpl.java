package com.seohee.online.service.order;

import com.seohee.common.dto.OrderDto;
import com.seohee.common.exception.RedisOperationException;
import com.seohee.common.exception.StockNotEnoughException;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.OrderProduct;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.User;
import com.seohee.domain.enums.DeliveryType;
import com.seohee.online.event.OrderCanceledEvent;
import com.seohee.online.event.OrderCreatedEvent;
import com.seohee.online.redis.publisher.StockDecreasePublisher;
import com.seohee.online.redis.publisher.StockRestorePublisher;
import com.seohee.online.redis.repository.StockAdjustRepository;
import com.seohee.online.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RedisOrderServiceImpl implements OrderService {

    private final OrderCommonService orderCommonService;

    private final OrderRepository orderRepository;
    private final StockAdjustRepository stockAdjustRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public OrderDto.OrderDetailResponse placeOrder(OrderDto.OrderRequest orderRequest) {
        User user = orderCommonService.getUser(orderRequest.userId());

        DeliveryType deliveryType = DeliveryType.valueOf(
                orderRequest.deliveryTypeRequest().name());

        Order order = new Order(user, deliveryType);

        order.changeOrderStatusToOrderPending();
        addProducts(orderRequest.orderProducts(), order);
        orderCommonService.validateTotalAmount(order.getTotalAmount(), orderRequest.totalAmount());

        Map<Long, Long> productQuantityMap = toProductQuantityMap(order);
        // Redis 선차감
        long start = System.currentTimeMillis();
        boolean isSuccess = stockAdjustRepository.decreaseStock(productQuantityMap);
        long end = System.currentTimeMillis();
        log.info("[Redis] stock decrease time: {} ms", end - start);
        if(!isSuccess) {
            throw new StockNotEnoughException();
        }
        orderRepository.save(order);

        // 주문 생성 이벤트 발행 (트랜잭션 커밋 이후 재고 차감 처리)
        applicationEventPublisher.publishEvent(
                new OrderCreatedEvent(order.getId(), productQuantityMap));

        return orderCommonService.toOrderDetailResponse(order);
    }

    @Transactional
    @Override
    public OrderDto.OrderDetailResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderCommonService.getSuccessOrder(orderId, userId);

        order.changeOrderStatusToCancelRequested();

        Map<Long, Long> productQuantityMap = toProductQuantityMap(order);
        // Redis 재고 복구
        boolean isSuccess = stockAdjustRepository.restoreStock(productQuantityMap);
        if(!isSuccess) {
            throw new RedisOperationException();
        }

        // pub으로 DB 재고 증가 + stockLog 생성
        applicationEventPublisher.publishEvent(
                new OrderCanceledEvent(order.getId(), productQuantityMap)
        );

        return orderCommonService.toOrderDetailResponse(order);
    }

    private void addProducts(
            List<OrderDto.OrderProductRequest> products, Order order) {
        for(OrderDto.OrderProductRequest opReq : products) {
            Product product = orderCommonService.getProduct(opReq.productId());
            long quantity = opReq.quantity();
            long unitPrice = opReq.unitPrice();

            OrderProduct orderProduct = OrderProduct.from(product, quantity, unitPrice);
            order.addOrderProduct(orderProduct);
        }
    }

    private Map<Long, Long> toProductQuantityMap(Order order) {
        Map<Long, Long> productQuantityMap = new HashMap<>();

        for(OrderProduct op : order.getOrderProducts()) {
            Long productId = op.getProduct().getId();
            long quantity = op.getQuantity();
            // key : 주문한 상품ID, value : 주문수량
            productQuantityMap.put(productId, quantity);
        }

        return productQuantityMap;
    }
}
