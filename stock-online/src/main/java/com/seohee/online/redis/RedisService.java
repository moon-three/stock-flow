package com.seohee.online.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean decreaseStockInRedis(Map<Long, Long> productMap) {
        List<String> keys = productMap.keySet().stream()
                .map(id -> "stock:" + id)
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

    public boolean restoreStockInRedis(Map<Long, Long> productMap) {
        List<String> keys = productMap.keySet().stream()
                .map(id -> "stock:" + id)
                .toList();

        String[] quantities =  productMap.values().stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        String luaScript = getLuaScriptForRestore();

        long result = (Long) redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                keys,
                (Object[]) quantities);

        if(result == -1) {
            log.error("Redis 재고 복구 실패");
        }

        return  result == 1L;
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
