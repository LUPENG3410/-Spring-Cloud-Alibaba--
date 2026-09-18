package com.changxing.admin.dto;

import lombok.Data;

@Data
public class MaintenanceReminderDTO {

    private Long carId;
    private String plateNumber;
    private String carName;
    private Long storeId;
    private String storeName;
    private String storePhone;
    private String lastMaintenance;
    private String nextMaintenanceDate;
    private Integer daysRemaining;
    private String status;
}
