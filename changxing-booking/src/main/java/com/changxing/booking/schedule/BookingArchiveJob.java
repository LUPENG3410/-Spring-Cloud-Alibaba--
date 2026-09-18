package com.changxing.booking.schedule;

import com.changxing.booking.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 冷热数据分离：终态历史订单归档，主表只保留活跃订单，减小热点查询扫表范围
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingArchiveJob {

    private final BookingMapper bookingMapper;

    // 每天凌晨 4:30 执行一次
    @Scheduled(cron = "0 30 4 * * ?")
    public void archive() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(90);
        int archived = bookingMapper.archiveBefore(deadline);
        if (archived > 0) {
            int deleted = bookingMapper.deleteBefore(deadline);
            log.info("订单归档完成: 归档 {} 条, 删除 {} 条", archived, deleted);
        }
    }
}