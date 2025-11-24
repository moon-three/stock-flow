package com.seohee.online.service;

import com.seohee.common.dto.OrderDto;
import com.seohee.common.dto.DeliveryTypeRequest;
import com.seohee.common.exception.DeliveryTypeNotSelectedException;
import com.seohee.common.exception.ProductNotExistException;
import com.seohee.common.exception.StockNotEnoughException;
import com.seohee.common.exception.StockNotFoundException;
import com.seohee.common.exception.TotalAmountMismatchException;
import com.seohee.common.exception.UserNotFoundException;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.OrderProduct;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.Stock;
import com.seohee.domain.entity.StockLog;
import com.seohee.domain.entity.User;
import com.seohee.domain.enums.DeliveryType;
import com.seohee.domain.enums.StockChangeType;
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.StockLogRepository;
import com.seohee.online.repository.StockRepository;
import com.seohee.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final StockLogRepository stockLogRepository;

    @Transactional
    @Override
    public OrderDto.OrderResponse placeOrder(OrderDto.OrderRequest orderRequest) {
        User user = getUser(orderRequest.userId());
        DeliveryType deliveryType = toDomain(orderRequest.deliveryTypeRequest());

        Order order = new Order(user, deliveryType);

        addProductsToOrder(orderRequest.orderProducts(), order);

        order.calculateTotalAmount();
        checkTotalAmount(order.getTotalAmount(), orderRequest.totalAmount());

        orderRepository.save(order);

        return toOrderResponse(order);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
    }

    // request의 enum객체를 entity의 enum객체로 변환
    private DeliveryType toDomain(DeliveryTypeRequest dtReq) {
        if(dtReq == null) {
            throw new DeliveryTypeNotSelectedException();
        }
        return DeliveryType.valueOf(dtReq.name());
    }

    private void addProductsToOrder(
                List<OrderDto.OrderProductRequest> products, Order order) {
        for(OrderDto.OrderProductRequest opReq : products) {
            Long productId = opReq.productId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotExistException());
            long quantity = opReq.quantity();
            long unitPrice = opReq.unitPrice();

            OrderProduct orderProduct = OrderProduct.from(product, quantity, unitPrice);
            order.addOrderProducts(orderProduct);

            // 재고 차감 (일단 온라인배송인 경우만 구현)
            if(order.getDeliveryType() == DeliveryType.ONLINE) {
                decreaseStock(productId, quantity);
            }
        }
    }

    // 프론트 쪽에서 계산해서 넘겨준 totalAmount와 엔티티에 저장할 totalAmount가 일치하는지 확인
    public void checkTotalAmount(long totalAmount, long requestTotalAmount) {
        if(totalAmount != requestTotalAmount) {
            throw new TotalAmountMismatchException();
        }
    }

    private OrderDto.OrderResponse toOrderResponse(Order order) {
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

        return new OrderDto.OrderResponse(
                order.getId(),
                order.getOrderStatus().name(),
                order.getDeliveryType().name(),
                orderProductInfos,
                order.getTotalAmount()
        );
    }

    private void decreaseStock(Long productId, long quantity) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new StockNotFoundException());

        if(stock.getQuantity() < quantity) {
            throw new StockNotEnoughException();
        }
        stock.decreaseQuantity(quantity);

        StockLog stockLog = StockLog.from(stock, quantity, StockChangeType.ORDER);
        stockLogRepository.save(stockLog);
    }
}
