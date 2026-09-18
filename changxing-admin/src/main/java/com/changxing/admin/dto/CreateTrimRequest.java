package com.changxing.admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateTrimRequest {
    private String brand;
    private String seriesName;
    private String name;
    private String type;
    private Integer year;
    private String image;
    private Double price;
    private Double rentalPrice;
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
    private Boolean reversingCamera;
    private Boolean autoHold;
    private String driverAssist;
    private String smartConnect;
    private Boolean electricSeat;
    private String seatFunction;
    private List<String> images;
    private List<String> interiorImages;
    private List<String> features;
    private List<String> descriptions;
}
