package com.changxing.booking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingCreateRequest {

    private Long carId;

    private Long trimId;

    private String carName;

    private String carImage;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime pickupTime;

    private LocalDateTime returnTime;

    private Integer totalDays;

    private BigDecimal totalPrice;

    private Long pickupStoreId;

    private Long returnStoreId;

    private String pickupLocation;

    private String returnLocation;

    private String pickupProvince;

    private String returnProvince;

    private Long insuranceProductId;
}
