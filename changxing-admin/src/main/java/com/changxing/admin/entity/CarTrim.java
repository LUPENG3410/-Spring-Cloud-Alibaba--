package com.changxing.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("car_trims")
public class CarTrim {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String brand;
    private String seriesName;
    private String name;
    private String type;
    private Integer year;
    private String image;
    private BigDecimal price;
    private BigDecimal rentalPrice;
    private Integer seats;
    private String fuel;
    private String transmission;
    private String engine;
    private Integer horsepower;
    private Integer torque;
    private String displacement;
    private String acceleration;
    private String topSpeed;
    private String fuelConsumption;
    private String wheelbase;
    private String length;
    private String width;
    private String height;
    private String trunkVolume;
    private String weight;
    private String fuelType;
    private String fuelGrade;
    private String fuelTankCapacity;
    private String doors;
    private Integer reversingCamera;
    private Integer autoHold;
    private String driverAssist;
    private String smartConnect;
    private Integer electricSeat;
    private String seatFunction;
    private String images;
    private String interiorImages;
    private String features;
    private String descriptions;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
