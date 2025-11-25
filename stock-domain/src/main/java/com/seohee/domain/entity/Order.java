package com.seohee.domain.entity;

import com.seohee.domain.enums.DeliveryType;
import com.seohee.domain.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;

    private long totalAmount;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    public Order(User user, DeliveryType deliveryType) {
        this.user = user;
        this.deliveryType = deliveryType;
        this.orderProducts = new ArrayList<>();
        this.orderStatus = OrderStatus.SUCCESS;     // 동기로 처리할때는 바로 SUCCESS
    }

    public void addOrderProducts(OrderProduct orderProduct) {
        orderProducts.add(orderProduct);
        orderProduct.setOrder(this);
    }

    public void calculateTotalAmount() {
        long calculated = 0;
        for(OrderProduct orderProduct : orderProducts) {
            calculated += orderProduct.getSubTotal();
        }
        this.totalAmount = calculated;
    }

    public void changeOrderStatusToCancel() {
        this.orderStatus = OrderStatus.CANCEL;
    }
}
