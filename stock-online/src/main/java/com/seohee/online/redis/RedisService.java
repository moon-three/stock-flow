package com.seohee.online.redis;

import com.seohee.common.exception.RedisOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
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

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                keys,
                (Object[]) quantities);

        if(result == null) {
            throw new RedisOperationException();
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

        Long result = (Long) redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                keys,
                (Object[]) quantities);

        return result == 1L;
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
                    redis.call('DECRBY', key, ARGV[i])
                end
                
                return 1
                """;
    }

    private String getLuaScriptForRestore() {
        return """
                for i, key in ipairs(KEYS) do
                    redis.call('INCRBY', key, ARGV[i])
                end
                
                return 1
                """;
    }
}
