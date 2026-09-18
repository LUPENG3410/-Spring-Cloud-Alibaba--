package com.changxing.admin.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private String memberLevel;
    private String registerDate;
    private long totalOrders;
    private long totalSpent;
    private String status;
}
