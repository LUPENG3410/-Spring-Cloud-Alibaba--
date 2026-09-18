package com.changxing.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateCarRequest {
    private String name;
    private String brand;
    private String type;
    private String image;
    private Double price;
    private Integer seats;
    private String fuel;
    private String transmission;
    private Boolean available;
    private List<String> features;
    private Long storeId;
    private Integer mileage;
    private String lastMaintenance;
    private String status;
    private Integer year;
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
    private List<String> images;
    private List<String> interiorImages;
    private List<String> descriptions;
    private String fuelType;
    private String fuelGrade;
    private String fuelTankCapacity;
    private String doors;
    private Boolean reversingCamera;
    private Boolean autoHold;
    private String driverAssist;
    private String smartConnect;
    private Boolean electricSeat;
    private String seatFunction;
    private String plateProvince;
    private String plateLetter;
    private String plateNumber;
}
