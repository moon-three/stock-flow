package com.seohee.domain.entity;

import com.seohee.domain.enums.ChangeReason;
import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class StockLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private Long changeEntity;

    @Enumerated(EnumType.STRING)
    private ChangeReason changeReason;

    @LastModifiedDate
    private LocalDateTime updated_at;
}
