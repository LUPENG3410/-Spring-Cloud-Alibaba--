package com.changxing.car.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class TrimDetailDTO {
    private Long id;
    private String name;
    private String brand;
    private String seriesName;
    private String type;
    private Integer year;
    private String image;
    private BigDecimal price;
    private BigDecimal rentalPrice;
    private Integer seats;
    private String fuel;
    private String transmission;

    // 动力参数
    private String engine;
    private Integer horsepower;
    private Integer torque;
    private String displacement;

    // 性能参数
    private String acceleration;
    private String topSpeed;
    private String fuelConsumption;

    // 车身尺寸
    private String wheelbase;
    private String length;
    private String width;
    private String height;
    private String trunkVolume;
    private String weight;

    // 能源配置
    private String fuelType;
    private String fuelGrade;
    private String fuelTankCapacity;
    private String doors;

    // 智能配置
    private Boolean reversingCamera;
    private Boolean autoHold;
    private String driverAssist;
    private String smartConnect;
    private Boolean electricSeat;
    private String seatFunction;

    // Mapper接收数据库JSON字符串
    private String featuresStr;
    private String imagesStr;
    private String interiorImagesStr;
    private String descriptionsStr;
    // 最终返回前端数组
    private List<String> features;
    private List<String> images;
    private List<String> interiorImages;
    private List<String> descriptions;

    // 评价列表
    private List<Map<String, Object>> reviews;

    // 热门排行订单数
    private Integer orderCount;
}
