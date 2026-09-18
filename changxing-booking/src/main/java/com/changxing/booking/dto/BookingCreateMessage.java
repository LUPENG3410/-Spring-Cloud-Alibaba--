package com.changxing.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateMessage implements Serializable {
    private String orderNo;
    private Long userId;
    private BookingCreateRequest request;
}