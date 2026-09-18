package com.changxing.admin.dto;

import lombok.Data;

@Data
public class UpdateCarRequest {
    private Integer price;
    private Integer mileage;
    private String image;
}
