package com.changxing.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.changxing.booking.entity.Booking;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    // 冷热分离：终态历史订单迁入归档表（历史数据不再参与热点查询）
    @Insert("INSERT INTO bookings_archive SELECT * FROM bookings " +
            "WHERE status IN ('completed','cancelled') AND updated_at < #{deadline}")
    int archiveBefore(@Param("deadline") LocalDateTime deadline);

    @Delete("DELETE FROM bookings " +
            "WHERE status IN ('completed','cancelled') AND updated_at < #{deadline}")
    int deleteBefore(@Param("deadline") LocalDateTime deadline);
}
