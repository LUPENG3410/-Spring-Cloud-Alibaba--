package com.changxing.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long trimId;

    private Long bookingId;

    private Integer rating;

    private String content;

    private Integer carDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
