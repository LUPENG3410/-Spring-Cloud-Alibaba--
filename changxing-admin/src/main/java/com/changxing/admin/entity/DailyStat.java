package com.changxing.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_stats")
public class DailyStat implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate statDate;

    private Integer newUsers;

    private Integer newBookings;

    private Integer completedBookings;

    private Integer cancelledBookings;

    private BigDecimal revenue;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
