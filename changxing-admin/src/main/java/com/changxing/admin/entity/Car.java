package com.changxing.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cars")
public class Car {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long trimId;
    private Long storeId;
    private String plateNumber;
    private String province;
    private String city;
    private Integer mileage;
    private LocalDate lastMaintenance;
    private Integer maintenanceIntervalDays;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
