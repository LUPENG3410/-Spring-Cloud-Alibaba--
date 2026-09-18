package com.changxing.admin.dto;

import lombok.Data;

@Data
public class StoreDTO {
    private Long id;
    private String name;
    private String province;
    private String city;
    private String address;
    private String phone;
    private Double lat;
    private Double lng;
    private String hours;
    private long carCount;
    private String status;
}
