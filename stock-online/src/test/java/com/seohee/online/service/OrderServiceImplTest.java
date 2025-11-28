package com.seohee.online.service;

import com.seohee.common.dto.DeliveryTypeRequest;
import com.seohee.common.dto.OrderDto;
import com.seohee.common.exception.StockNotEnoughException;
import com.seohee.common.exception.TotalAmountMismatchException;
import com.seohee.domain.entity.Product;
import com.seohee.domain.entity.Stock;
import com.seohee.domain.entity.User;
import com.seohee.online.repository.ProductRepository;
import com.seohee.online.repository.StockRepository;
import com.seohee.online.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private StockRepository stockRepository;

    @Test
    void testOrderSuccess() {
        // given
        User user = userRepository.findById(1L).get();

        Product p1 = productRepository.findById(1L).get();
        Product p2 = productRepository.findById(2L).get();

        OrderDto.OrderProductRequest opReq1 = new OrderDto.OrderProductRequest(
                p1.getId(), 10, 1000);
        OrderDto.OrderProductRequest opReq2 = new OrderDto.OrderProductRequest(
                p2.getId(), 10, 2000);

        List<OrderDto.OrderProductRequest> orderProducts = List.of(opReq1, opReq2);

        long beforeP1Stock = stockRepository.findById(1L).get().getQuantity();
        long beforeP2Stock = stockRepository.findById(2L).get().getQuantity();
        long expectedTotalAmount = 10 * 1000 + 10 * 2000;

        OrderDto.OrderRequest orderRequest = new OrderDto.OrderRequest(
                user.getId(), orderProducts, expectedTotalAmount, DeliveryTypeRequest.ONLINE
        );

        // when
        OrderDto.OrderDetailResponse response = orderService.placeOrder(orderRequest);

        // then
        assertEquals(expectedTotalAmount, response.totalAmount());
        assertEquals("SUCCESS", response.orderStatus());

        Stock s1 = stockRepository.findById(1L).get();
        Stock s2 = stockRepository.findById(2L).get();

        assertEquals(beforeP1Stock - 10, s1.getQuantity());
        assertEquals(beforeP2Stock - 10, s2.getQuantity());
    }


    @Test
    void testOrderFail_notEnoughStock() {
        // given
        User user = userRepository.findById(1L).get();

        Product p1 = productRepository.findById(1L).get();

        OrderDto.OrderProductRequest opReq1 = new OrderDto.OrderProductRequest(
                p1.getId(), 500, 1000
        );

        long totalAmount = 500 * 1000;

        OrderDto.OrderRequest orderRequest = new OrderDto.OrderRequest(
                user.getId(), List.of(opReq1), totalAmount, DeliveryTypeRequest.ONLINE
        );

        // when
        // then
        assertThrows(StockNotEnoughException.class, () -> orderService.placeOrder(orderRequest));
    }

    @Test
    void testOrderFail_totalAmountMismatch() {
        // given
        User user = userRepository.findById(1L).get();

        Product p1 = productRepository.findById(1L).get();

        OrderDto.OrderProductRequest opReq1 = new OrderDto.OrderProductRequest(
                p1.getId(), 10, 1000
        );

        OrderDto.OrderRequest orderRequest = new OrderDto.OrderRequest(
                user.getId(), List.of(opReq1), 20000, DeliveryTypeRequest.ONLINE
        );

        // when
        // then
        assertThrows(TotalAmountMismatchException.class, () -> orderService.placeOrder(orderRequest));
    }

    @Test
    void testOrderCancel() {
        // given
        User user = userRepository.findById(1L).get();

        Product p1 = productRepository.findById(1L).get();

        OrderDto.OrderProductRequest opReq1 = new OrderDto.OrderProductRequest(
                p1.getId(), 10, 1000
        );

        long totalAmount = 10 * 1000;

        OrderDto.OrderRequest orderRequest = new OrderDto.OrderRequest(
                user.getId(), List.of(opReq1), totalAmount, DeliveryTypeRequest.ONLINE
        );

        OrderDto.OrderDetailResponse orderResponse = orderService.placeOrder(orderRequest);

        long stockBeforeCancel = stockRepository.findById(1L).get().getQuantity();

        // when
        OrderDto.OrderDetailResponse response = orderService.cancelOrder(orderResponse.orderId(), user.getId());
        long stockAfterCancel = stockRepository.findById(1L).get().getQuantity();

        // then
        assertEquals(stockBeforeCancel + 10, stockAfterCancel);
        assertEquals("CANCEL", response.orderStatus());
    }

}