package com.changxing.booking.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.booking.entity.Booking;
import com.changxing.booking.mapper.BookingMapper;
import com.changxing.booking.service.OccupancyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// 兜底对账：清理残留占用 + 超时未支付释放 + 以 MySQL 为准重建 Redis 占用
@Slf4j
@Component
@RequiredArgsConstructor
public class OccupancyReconcileJob {

    private static final Duration PENDING_TIMEOUT = Duration.ofMinutes(15);

    private final BookingMapper bookingMapper;
    private final OccupancyService occupancyService;

    // 1) 清理已取消/已完成订单残留的 Redis 占用（幂等）
    @Scheduled(fixedDelay = 300_000)
    public void reconcileTerminalBookings() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Booking::getStatus, "cancelled", "completed")
                .ge(Booking::getUpdatedAt, since);
        List<Booking> list = bookingMapper.selectList(wrapper);
        for (Booking b : list) {
            occupancyService.release(
                    b.getTrimId() != null ? b.getTrimId() : b.getCarId(),
                    b.getPickupProvince(), b.getStartDate(), b.getEndDate());
        }
    }

    // 2) 超时未支付释放：pending 超过 15 分钟未支付，取消并释放占用
    @Scheduled(fixedDelay = 60_000)
    public void releaseTimeoutPending() {
        LocalDateTime deadline = LocalDateTime.now().minus(PENDING_TIMEOUT);
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStatus, "pending")
                .le(Booking::getCreatedAt, deadline);
        List<Booking> list = bookingMapper.selectList(wrapper);
        for (Booking b : list) {
            b.setStatus("cancelled");
            b.setUpdatedAt(LocalDateTime.now());
            bookingMapper.updateById(b);
            occupancyService.release(
                    b.getTrimId() != null ? b.getTrimId() : b.getCarId(),
                    b.getPickupProvince(), b.getStartDate(), b.getEndDate());
            log.info("超时未支付订单已取消并释放占用: orderNo={}", b.getOrderNo());
        }
    }

    // 3) 全量对账：以 MySQL 活跃订单为准重建 Redis 占用集合，清理孤儿 key（最终一致性兜底）
    @Scheduled(fixedDelay = 600_000)
    public void rebuildFromDatabase() {
        try {
            LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Booking::getStatus, "pending", "confirmed", "active");
            List<Booking> actives = bookingMapper.selectList(wrapper);

            Map<String, List<Booking>> byKey = actives.stream()
                    .filter(b -> (b.getTrimId() != null || b.getCarId() != null)
                            && b.getStartDate() != null && b.getEndDate() != null)
                    .collect(Collectors.groupingBy(b -> occupancyService.keyOf(
                            b.getTrimId() != null ? b.getTrimId() : b.getCarId(),
                            b.getPickupProvince())));

            // 逐个 key 重建
            byKey.forEach((key, bookings) -> {
                occupancyService.deleteKey(key);
                for (Booking b : bookings) {
                    occupancyService.add(
                            b.getTrimId() != null ? b.getTrimId() : b.getCarId(),
                            b.getPickupProvince(), b.getStartDate(), b.getEndDate());
                }
            });

            // 清理 Redis 中存在、MySQL 已无活跃订单的孤儿 key
            Set<String> keys = occupancyService.scanKeys();
            for (String key : keys) {
                if (!byKey.containsKey(key)) {
                    occupancyService.deleteKey(key);
                    log.info("清理孤儿占用 key: {}", key);
                }
            }
        } catch (Exception e) {
            log.error("Redis 占用对账失败: {}", e.getMessage(), e);
        }
    }
}