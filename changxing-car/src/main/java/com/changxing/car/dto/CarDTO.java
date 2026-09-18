package com.changxing.car.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CarDTO {
    private Long id;
    private String name;
    private String brand;
    private String type;
    private String image;
    private BigDecimal price;
    private BigDecimal rentalPrice;
    private Integer seats;
    private String fuel;
    private String transmission;
    private String province;
    private Boolean available;

    // 顺风车字段
    private String currentProvince;
    private String homeProvince;
    private Boolean isHitch;

    // Mapper接收数据库JSON字符串
    private String featuresStr;
    // 最终返回前端数组
    private List<String> features;
}