package com.changxing.admin.dto;

import lombok.Data;

@Data
public class BookingDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private Long carId;
    private String carName;
    private String carImage;
    private String startDate;
    private String endDate;
    private String pickupTime;
    private String returnTime;
    private Integer totalDays;
    private Integer totalPrice;
    private String status;
    private String pickupLocation;
    private String returnLocation;
    private String pickupProvince;
    private String returnProvince;
    private Long insuranceProductId;
    private String insuranceName;
    private Integer insurancePrice;
    private String createdAt;
}
