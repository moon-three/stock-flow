package com.seohee.domain.entity;

import com.seohee.domain.enums.StockChangeReason;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@EntityListeners(AuditingEntityListener.class)
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Long quantity;

    @Enumerated(EnumType.STRING)
    private StockChangeReason stockChangeReason;

    @LastModifiedDate
    private LocalDateTime updated_at;
}
