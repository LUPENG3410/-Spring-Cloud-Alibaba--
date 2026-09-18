package com.changxing.admin.dto;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private long totalCars;
    private long availableCars;
    private long totalBookings;
    private long pendingBookings;
    private long totalUsers;
    private long totalStores;
    private long monthlyRevenue;
    private long todayBookings;
}
