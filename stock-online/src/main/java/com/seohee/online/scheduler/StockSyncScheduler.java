package com.seohee.online.scheduler;

import com.seohee.domain.entity.Stock;
import com.seohee.online.redis.repository.RedisStockRepository;
import com.seohee.online.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile( "!test")
@Slf4j
public class StockSyncScheduler {

    private final StockRepository stockRepository;
    private final RedisStockRepository redisStockRepository;

    @Scheduled(cron = "${scheduler.stock-sync.cron}")
    public void stockSyncScheduling() {
        log.info("Redis 재고 동기화 시작");
        List<Stock> deletedStocks = stockRepository.findAllDeletedStocks();
        List<Stock> stocks = stockRepository.findAllValidStocks();

        for(Stock stock : deletedStocks) {
            redisStockRepository.deleteStock(stock);
        }

        for(Stock stock : stocks) {
            redisStockRepository.setStock(stock);
        }
        log.info("Redis 재고 동기화 끝");
    }
}
