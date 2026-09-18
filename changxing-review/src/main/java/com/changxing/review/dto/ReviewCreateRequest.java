package com.changxing.review.dto;

import lombok.Data;

@Data
public class ReviewCreateRequest {
    private Long trimId;
    private Long bookingId;
    private Integer rating;
    private String content;
    private Integer carDays;
}
