package com.seohee.online.service.order;

import com.seohee.common.dto.OrderDto;
import com.seohee.common.exception.StockNotEnoughException;
import com.seohee.common.exception.StockNotFoundException;
import com.seohee.domain.entity.Order;
import com.seohee.domain.entity.OrderProduct;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.Stock;
import com.seohee.domain.entity.StockLog;
import com.seohee.domain.entity.User;
import com.seohee.domain.enums.DeliveryType;
import com.seohee.domain.enums.StockChangeType;
import com.seohee.online.repository.OrderRepository;
import com.seohee.online.repository.StockLogRepository;
import com.seohee.online.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderCommonService orderCommonService;

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final StockLogRepository stockLogRepository;

    @Transactional
    @Override
    public OrderDto.OrderDetailResponse placeOrder(OrderDto.OrderRequest orderRequest) {
        User user = orderCommonService.getUser(orderRequest.userId());

        // request의 enum객체를 entity의 enum객체로 변환
        DeliveryType deliveryType = DeliveryType.valueOf(
                orderRequest.deliveryTypeRequest().name());

        Order order = new Order(user, deliveryType);
        // 동기로 처리할때는 바로 SUCCESS
        order.changeOrderStatusToSuccess();
        addProductsAndDecreaseStock(orderRequest.orderProducts(), order);
        orderCommonService.validateTotalAmount(order.getTotalAmount(), orderRequest.totalAmount());

        orderRepository.save(order);

        return orderCommonService.toOrderDetailResponse(order);
    }

    @Transactional
    @Override
    public OrderDto.OrderDetailResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderCommonService.getSuccessOrder(orderId, userId);

        restoreStockForOrder(order);
        order.changeOrderStatusToCancel();

        return orderCommonService.toOrderDetailResponse(order);
    }

    private void addProductsAndDecreaseStock(
                List<OrderDto.OrderProductRequest> products, Order order) {
        for(OrderDto.OrderProductRequest opReq : products) {
            Long productId = opReq.productId();
            Product product = orderCommonService.getProduct(productId);

            long quantity = opReq.quantity();
            long unitPrice = opReq.unitPrice();

            OrderProduct orderProduct = OrderProduct.from(product, quantity, unitPrice);
            order.addOrderProduct(orderProduct);

            // 재고 차감 (일단 온라인배송인 경우만 구현)
            if(order.getDeliveryType() == DeliveryType.ONLINE) {
                decreaseStockForOrderProduct(productId, quantity);
            }
        }
    }

    private void decreaseStockForOrderProduct(Long productId, long quantity) {
        long start = System.currentTimeMillis();
        Stock stock = getStock(productId);

        if(stock.getQuantity() < quantity) {
            throw new StockNotEnoughException();
        }
        stock.decreaseQuantity(quantity);
        long end = System.currentTimeMillis();
        log.info("[DB] lock + decrease time: {} ms", end - start);
        StockLog stockLog = StockLog.from(stock, quantity, StockChangeType.ORDER);
        stockLogRepository.save(stockLog);
    }

    private void restoreStockForOrder(Order order) {
        for(OrderProduct op : order.getOrderProducts()) {
            Stock stock = getStock(op.getProduct().getId());

            long quantityToCancel = op.getQuantity();

            stock.increaseQuantity(quantityToCancel);

            StockLog stockLog = StockLog.from(stock, quantityToCancel, StockChangeType.CANCEL);
            stockLogRepository.save(stockLog);
        }
    }

    private Stock getStock(Long productId) {
        return stockRepository.findByProductForDecreasing(productId)
                .orElseThrow(() -> new StockNotFoundException());
    }
}
