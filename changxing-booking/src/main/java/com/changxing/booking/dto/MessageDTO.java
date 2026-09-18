package com.changxing.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {
    
    private Long bookingId;
    private String orderNo;
    private Long userId;
    private Long carId;
    private Long trimId;
    private String status;
    private String carName;
    private Integer totalDays;
    private String pickupProvince;
    private String returnProvince;
    private LocalDateTime timestamp;
    private String message;
}
