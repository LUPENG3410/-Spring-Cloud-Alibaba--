package com.changxing.car.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
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
    private String lastMaintenance;
    private String status;
    private LocalDateTime deletedAt;
}