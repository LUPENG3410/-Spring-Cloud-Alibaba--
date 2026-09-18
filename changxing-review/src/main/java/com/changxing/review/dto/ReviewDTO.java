package com.changxing.review.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String avatar;
    private Long trimId;
    private Integer rating;
    private String content;
    private Integer carDays;
    private LocalDateTime createdAt;
}
