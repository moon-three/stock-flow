package com.seohee.online.service.order;

import com.seohee.common.dto.OrderDto;
import com.seohee.common.exception.InvalidOrderStatusException;
import com.seohee.common.exception.OrderNotExistException;
import com.seohee.common.exception.ProductNotExistException;
import com.seohee.common.exception.TotalAmountMismatchException;
import com.seohee.common.exception.UserMismatchException;
import com.seohee.common.exception.UserNotFoundException;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.User;
import com.seohee.domain.enums.OrderStatus;
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCommonService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
    }

    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotExistException());
    }

    public Order getSuccessOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotExistException());

        if(!order.getUser().getId().equals(userId)) {
            throw new UserMismatchException();
        }
        if(order.getOrderStatus() != OrderStatus.SUCCESS) {
            throw new InvalidOrderStatusException();
        }
        return order;
    }

    // 프론트 쪽에서 계산해서 넘겨준 totalAmount와 엔티티에 저장할 totalAmount가 일치하는지 확인
    public void validateTotalAmount(long totalAmount, long requestTotalAmount) {
        if(totalAmount != requestTotalAmount) {
            throw new TotalAmountMismatchException();
        }
    }

    public OrderDto.OrderDetailResponse toOrderDetailResponse(Order order) {
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
