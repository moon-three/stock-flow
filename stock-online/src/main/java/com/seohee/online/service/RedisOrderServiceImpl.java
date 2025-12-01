package com.seohee.online.service;

import com.seohee.common.dto.OrderDto;
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
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisOrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private final RedisService redisService;

    @Override
    public OrderDto.OrderDetailResponse placeOrder(OrderDto.OrderRequest orderRequest) {
        User user = getUser(orderRequest.userId());

        DeliveryType deliveryType = DeliveryType.valueOf(
                orderRequest.deliveryTypeRequest().name());

        OrderStatus orderStatus = OrderStatus.PENDING;

        Order order = new Order(user, deliveryType, orderStatus);

        HashMap<Long, Long> productMap = addProductsToOrder(
                orderRequest.orderProducts(), order);

        checkTotalAmount(order.getTotalAmount(), orderRequest.totalAmount());

        // Redis 선차감
        boolean isSuccess = redisService.decreaseStockInRedis(productMap);

        if(!isSuccess) {
            throw new StockNotEnoughException();
        }

        // TODO : 성공하면 pub/sub (DB 재고 차감 + StockLog 생성)

        orderRepository.save(order);

        return  toOrderDetailResponse(order);
    }

    @Override
    public OrderDto.OrderDetailResponse cancelOrder(Long orderId, Long userId) {
        return null;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
    }

    private HashMap<Long, Long> addProductsToOrder(
            List<OrderDto.OrderProductRequest> products, Order order) {
        HashMap<Long, Long> productMap = new HashMap<>();

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
