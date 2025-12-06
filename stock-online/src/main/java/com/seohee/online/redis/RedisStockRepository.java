package com.seohee.online.redis;

import com.seohee.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Slf4j
@Profile("!test")
@Repository
@RequiredArgsConstructor
public class RedisStockRepository implements StockCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final String STOCK_KEY_PREFIX = "stock:";

    @Override
    public boolean decreaseStock(Map<Long, Long> productMap) {
        log.info("redis 재고 선차감 시작");

        List<String> keys = productMap.keySet().stream()
                .map(id -> STOCK_KEY_PREFIX + id)
                .toList();

        String[] quantities =  productMap.values().stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        String luaScript = getLuaScriptForDecrease();

        long result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                keys,
                (Object[]) quantities);

        if(result == -1) {
            log.error("Redis 재고 차감 실패");
        }

        return result == 1L;
    }

    @Override
    public boolean restoreStock(Map<Long, Long> productMap) {
        log.info("redis 재고 선증가 시작");

        List<String> keys = productMap.keySet().stream()
                .map(id -> STOCK_KEY_PREFIX + id)
                .toList();

        String[] quantities =  productMap.values().stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        String luaScript = getLuaScriptForRestore();

        long result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                keys,
                (Object[]) quantities);

        if(result == -1) {
            log.error("Redis 재고 복구 실패");
        }

        return  result == 1L;
    }

    public void initStock(Stock stock) {
        redisTemplate.opsForValue().set(
                STOCK_KEY_PREFIX + stock.getProduct().getId(),
                stock.getQuantity() + ""
        );
    }


    private String getLuaScriptForDecrease() {
        return """
                for i, key in ipairs(KEYS) do
                    local stock = tonumber(redis.call('GET', key))
                    local quantityForOrder = tonumber(ARGV[i])
                
                    if stock == nil then
                        return -1
                    end
                
                    if stock < quantityForOrder then
                        return -1
                    end
                end
                
                for i, key in ipairs(KEYS) do
                    local value = redis.call('DECRBY', key, ARGV[i])
                    if not value then
                        return -1
                    end
                end
                
                return 1
                """;
    }

    private String getLuaScriptForRestore() {
        return """
                for i, key in ipairs(KEYS) do
                    local value = redis.call('INCRBY', key, ARGV[i])
                    if not value then
                        return -1
                    end
                end
                
                return 1
                """;
    }
}
