package com.changxing.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.booking.entity.Booking;
import com.changxing.booking.mapper.BookingMapper;
import com.changxing.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

// 独立承载下单落库的"短事务"，避开 Service 自调用导致 @Transactional 失效
@Service
@RequiredArgsConstructor
public class BookingPersistService {

    private final BookingMapper bookingMapper;

    @Transactional
    public Booking createPending(Booking booking, Long trimId, String province) {
        // DB 唯一兜底校验 + insert 在同一事务内，作为超卖最后一道防线
        if (!isAvailable(trimId, province, booking.getStartDate(), booking.getEndDate())) {
            throw new BusinessException("该车辆在选定时间段内已被预约");
        }
        bookingMapper.insert(booking);
        return booking;
    }

    public boolean isAvailable(Long trimId, String pickupProvince, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getTrimId, trimId)
                .eq(StringUtils.hasText(pickupProvince), Booking::getPickupProvince, pickupProvince)
                .notIn(Booking::getStatus, "cancelled", "completed")
                .and(w -> w
                        .between(Booking::getStartDate, startDate, endDate)
                        .or()
                        .between(Booking::getEndDate, startDate, endDate)
                        .or()
                        .le(Booking::getStartDate, startDate)
                        .ge(Booking::getEndDate, endDate)
                );
        return bookingMapper.selectCount(wrapper) == 0;
    }
}