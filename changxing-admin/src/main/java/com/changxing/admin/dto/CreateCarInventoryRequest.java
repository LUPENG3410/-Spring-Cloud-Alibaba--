package com.changxing.admin.dto;

import lombok.Data;

@Data
public class CreateCarInventoryRequest {
    private Long trimId;
    private Long storeId;
    private String plateProvince;
    private String plateLetter;
    private String plateNumber;
    private Integer mileage;
    private String lastMaintenance;
    private String status;
}
