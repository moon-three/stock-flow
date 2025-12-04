package com.seohee.online.service;

import com.seohee.common.dto.OrderDto;
import com.seohee.common.exception.InvalidOrderStatusException;
import com.seohee.common.exception.OrderNotExistException;
import com.seohee.common.exception.ProductNotExistException;
import com.seohee.common.exception.StockNotEnoughException;
import com.seohee.common.exception.TotalAmountMismatchException;
import com.seohee.common.exception.UserNotFoundException;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.OrderProduct;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.User;
import com.seohee.domain.enums.DeliveryType;
import com.seohee.domain.enums.OrderStatus;
import com.seohee.online.redis.RedisService;
import com.seohee.online.redis.dto.StockDecreaseMessage;
import com.seohee.online.redis.dto.StockRestoreMessage;
import com.seohee.online.redis.publisher.StockDecreasePublisher;
import com.seohee.online.redis.publisher.StockRestorePublisher;
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisOrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private final RedisService redisService;
    private final StockDecreasePublisher stockDecreasePublisher;
    private final StockRestorePublisher stockRestorePublisher;

    @Transactional
    @Override
    public OrderDto.OrderDetailResponse placeOrder(OrderDto.OrderRequest orderRequest) {
        User user = getUser(orderRequest.userId());

        DeliveryType deliveryType = DeliveryType.valueOf(
                orderRequest.deliveryTypeRequest().name());

        Order order = new Order(user, deliveryType);
        order.changeOrderStatusToOrderPending();

        Map<Long, Long> productMap = addProductsToOrder(
                orderRequest.orderProducts(), order);

        checkTotalAmount(order.getTotalAmount(), orderRequest.totalAmount());

        // Redis 선차감
        boolean isSuccess = redisService.decreaseStockInRedis(productMap);

        if(!isSuccess) {
            throw new StockNotEnoughException();
        }

        orderRepository.save(order);

        StockDecreaseMessage messageDto = new StockDecreaseMessage(order.getId(), productMap);
        stockDecreasePublisher.publishAsync(messageDto);

        return toOrderDetailResponse(order);
    }

    @Transactional
    @Override
    public OrderDto.OrderDetailResponse cancelOrder(Long orderId, Long userId) {
        User user = getUser(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistException());

        if(order.getOrderStatus() != OrderStatus.SUCCESS) {
            throw new InvalidOrderStatusException();
        }

        order.changeOrderStatusToCancelRequested();

        // order 안의 orderProducts를 Map<productId, quantity> 으로 변경
        Map<Long, Long> productMap = toProductMap(order);

        // Redis 재고 복구
        redisService.restoreStockInRedis(productMap);

        // pub으로 DB 증감 + stockLog 생성
        StockRestoreMessage messageDto = new StockRestoreMessage(orderId, productMap);
        stockRestorePublisher.publishAsync(messageDto);

        return toOrderDetailResponse(order);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
    }

    private Map<Long, Long> addProductsToOrder(
            List<OrderDto.OrderProductRequest> products, Order order) {
        Map<Long, Long> productMap = new HashMap<>();

        for(OrderDto.OrderProductRequest opReq : products) {
            Long productId = opReq.productId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotExistException());

            long quantity = opReq.quantity();
            long unitPrice = opReq.unitPrice();

            OrderProduct orderProduct = OrderProduct.from(product, quantity, unitPrice);
            order.addOrderProduct(orderProduct);

            // key : 주문한 상품ID, value : 주문수량
            productMap.put(product.getId(), quantity);
        }

        return productMap;
    }

    public void checkTotalAmount(long totalAmount, long requestTotalAmount) {
        if(totalAmount != requestTotalAmount) {
            throw new TotalAmountMismatchException();
        }
    }

    private Map<Long, Long> toProductMap(Order order) {
        Map<Long, Long> productMap = new HashMap<>();

        for(OrderProduct op : order.getOrderProducts()) {
            Long productId = op.getProduct().getId();
            long quantity = op.getQuantity();

            productMap.put(productId, quantity);
        }

        return productMap;
    }

    private OrderDto.OrderDetailResponse toOrderDetailResponse(Order order) {
        List<OrderDto.OrderProductInfo> orderProductInfos = order.getOrderProducts()
                .stream()
                .map( op -> new OrderDto.OrderProductInfo(
                                op.getProduct().getId(),
                                op.getProduct().getName(),
                                op.getQuantity(),
                                op.getUnitPrice(),
                                op.getSubTotal()
                        )
                ).toList();

        return new OrderDto.OrderDetailResponse(
                order.getId(),
                order.getOrderStatus().name(),
                order.getDeliveryType().name(),
                orderProductInfos,
                order.getTotalAmount()
        );
    }
}
