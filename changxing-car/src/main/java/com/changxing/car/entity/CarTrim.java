package com.changxing.car.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

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
    private String fuelGrade;
    private String transmission;
    private String features;
    private Integer status;
}