package com.seohee.domain.entity;

import com.seohee.domain.enums.StockChangeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private long changeQuantity;

    @Enumerated(EnumType.STRING)
    private StockChangeType stockChangeType;

    @LastModifiedDate
    private LocalDateTime updated_at;

    public static StockLog from(Stock stock,
                                long changeQuantity,
                                StockChangeType stockChangeType) {
        return StockLog.builder()
                .stock(stock)
                .changeQuantity(changeQuantity)
                .stockChangeType(stockChangeType)
                .build();
    }
}
