package com.changxing.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("bookings")
public class Booking implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long carId;

    private Long trimId;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime pickupTime;

    private LocalDateTime returnTime;

    private Integer totalDays;

    private BigDecimal dailyPrice;

    private BigDecimal totalPrice;

    private BigDecimal deposit;

    private BigDecimal crossProvinceFee;

    private Long pickupStoreId;

    private Long returnStoreId;

    private String pickupProvince;

    private String returnProvince;

    private String status;

    private Long insuranceProductId;

    private BigDecimal insurancePrice;

    private String insuranceCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
