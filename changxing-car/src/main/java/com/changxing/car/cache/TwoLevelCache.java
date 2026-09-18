package com.changxing.car.cache;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

// L1 Caffeine 本地缓存 + L2 Redis 共享缓存；命中顺序 本地 -> Redis -> DB
@Component
@RequiredArgsConstructor
public class TwoLevelCache {

    private final Cache<String, Object> caffeine;
    private final StringRedisTemplate redisTemplate;

    public <T> List<T> getList(String key, Class<T> elementType, long remoteTtlSeconds, Supplier<List<T>> loader) {
        Object local = caffeine.getIfPresent(key);
        if (local instanceof List) {
            return (List<T>) local;
        }

        String json = redisTemplate.opsForValue().get(key);
        if (json != null && !json.isEmpty()) {
            List<T> list = JSON.parseArray(json, elementType);
            if (list != null) {
                caffeine.put(key, list);
                return list;
            }
        }

        List<T> loaded = loader.get();
        if (loaded != null) {
            caffeine.put(key, loaded);
            // 空列表也缓存防穿透；远程 TTL 加随机抖动防雪崩
            long ttl = remoteTtlSeconds + ThreadLocalRandom.current().nextLong(120);
            redisTemplate.opsForValue().set(key, JSON.toJSONString(loaded), ttl, TimeUnit.SECONDS);
        }
        return loaded;
    }

    public void evict(String key) {
        caffeine.invalidate(key);
        redisTemplate.delete(key);
    }

    public void evictByPrefix(String prefix) {
        caffeine.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(200).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                redisTemplate.delete(cursor.next());
            }
        }
    }
}