package com.changxing.booking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingDTO {

    private Long id;

    private String orderNo;

    private Long userId;

    private String userName;

    private String userPhone;

    private Long carId;

    private String carName;

    private String carImage;

    private BigDecimal dailyPrice;

    private BigDecimal totalPrice;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime pickupTime;

    private LocalDateTime returnTime;

    private Integer totalDays;

    private String pickupLocation;

    private String returnLocation;

    private String pickupProvince;

    private String returnProvince;

    private String status;

    private Long insuranceProductId;

    private String insuranceName;

    private BigDecimal insurancePrice;

    private LocalDateTime createdAt;
}
