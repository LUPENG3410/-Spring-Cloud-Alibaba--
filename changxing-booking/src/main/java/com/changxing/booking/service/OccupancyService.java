package com.changxing.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// 维护 (trimId, province) 维度的日期段占用，替代全局分布式锁排队
@Service
@RequiredArgsConstructor
public class OccupancyService {

    private static final String KEY_PREFIX = "occ:trim:";

    private static final DefaultRedisScript<Long> OCCUPY_SCRIPT = new DefaultRedisScript<>();

    static {
        OCCUPY_SCRIPT.setLocation(new ClassPathResource("lua/occupy.lua"));
        OCCUPY_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;

    public boolean tryOccupy(Long trimId, String province, LocalDate startDate, LocalDate endDate) {
        String key = buildKey(trimId, province);
        long startEpoch = startDate.toEpochDay();
        long endEpoch = endDate.toEpochDay();
        String member = startEpoch + "~" + endEpoch;

        Long result = redisTemplate.execute(
                OCCUPY_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(startEpoch),
                String.valueOf(endEpoch),
                member);
        return result != null && result == 1L;
    }

    public void release(Long trimId, String province, LocalDate startDate, LocalDate endDate) {
        String key = buildKey(trimId, province);
        String member = startDate.toEpochDay() + "~" + endDate.toEpochDay();
        redisTemplate.opsForZSet().remove(key, member);
    }

    // 对账用：直接写入一条占用（重建 Redis 时使用）
    public void add(Long trimId, String province, LocalDate startDate, LocalDate endDate) {
        String key = buildKey(trimId, province);
        String member = startDate.toEpochDay() + "~" + endDate.toEpochDay();
        redisTemplate.opsForZSet().add(key, member, endDate.toEpochDay());
    }

    // 对账用：清空某个 (trimId, province) 的全部占用
    public void clear(Long trimId, String province) {
        redisTemplate.delete(buildKey(trimId, province));
    }

    // 对账用：删除任意原始 key
    public void deleteKey(String rawKey) {
        redisTemplate.delete(rawKey);
    }

    // 对账用：扫描所有占用 key（用 SCAN，避免 KEYS 阻塞 Redis）
    public Set<String> scanKeys() {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(100).build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return keys;
        });
    }

    public String keyOf(Long trimId, String province) {
        return buildKey(trimId, province);
    }

    private String buildKey(Long trimId, String province) {
        String p = province == null ? "" : province;
        return KEY_PREFIX + trimId + ":" + p;
    }
}