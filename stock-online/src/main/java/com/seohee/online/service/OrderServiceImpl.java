package com.seohee.online.service;

import com.seohee.common.dto.OrderDto;
import com.seohee.common.dto.DeliveryTypeRequest;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.OrderProduct;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.User;
import com.seohee.domain.enums.DeliveryType;
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public OrderDto.OrderResponse placeOrder(OrderDto.OrderRequest orderRequest) {
        User user = getUser(orderRequest.userId());
        DeliveryType deliveryType = toDomain(orderRequest.deliveryTypeRequest());

        Order order = new Order(user, deliveryType);

        addProductsToOrder(orderRequest.orderProducts(), order);

        // totalAmount
        order.calculateTotalAmount();
        order.checkTotalAmount(orderRequest.totalAmount());

        // Order save (with OrderProduct)
        orderRepository.save(order);


        return toOrderResponse(order);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("찾을 수 없는 사용자입니다."));
    }

    private DeliveryType toDomain(DeliveryTypeRequest dtReq) {
        if(dtReq == null) {
            throw new RuntimeException("배송 방식을 선택해주세요.");
        }
        return DeliveryType.valueOf(dtReq.name());
    }

    private void addProductsToOrder(
                List<OrderDto.OrderProductRequest> products, Order order) {
        for(OrderDto.OrderProductRequest opReq : products) {
            Long productId = opReq.productId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException(
                            "존재하지 않거나 삭제된 상품입니다."));
            Long quantity = opReq.quantity();
            Long unitPrice = opReq.unitPrice();

            OrderProduct orderProduct = OrderProduct.from(
                    product, quantity, unitPrice);

            order.addOrderProducts(orderProduct);

            // TODO
            // OrderType check general: warehouseStock / today-pickup: storeStock
        }
    }

    private OrderDto.OrderResponse toOrderResponse(Order order) {
        List<OrderDto.OrderProductInfo> orderProductInfos = order.getOrderProducts()
                .stream()
                .map( op -> new OrderDto.OrderProductInfo(
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
}
