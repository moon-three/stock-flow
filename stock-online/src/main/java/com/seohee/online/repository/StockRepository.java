package com.seohee.online.repository;

import com.seohee.domain.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(Long productId);

    // 삭제되지 않은 상품의 재고들만 조회 (상품 정보 포함 x)
    @Query("select s from Stock s where s.product.isDeleted = false")
    List<Stock> findValidStocks();

    @Query("select s from Stock s where s.product.isDeleted = true")
    List<Stock> findDeletedStocks();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.product.id = :productId")
    Optional<Stock> findByProductForDecreasing(Long productId);

    @Modifying
    @Query("update Stock s set s.quantity = s.quantity - :quantity " +
            "where s.product.id = :productId and s.quantity >= :quantity")
    int decreaseQuantity(Long productId, Long quantity);

    @Modifying
    @Query("update Stock s set s.quantity = s.quantity + :quantity " +
            "where s.product.id = :productId")
    int increaseQuantity(Long productId, Long quantity);
}
