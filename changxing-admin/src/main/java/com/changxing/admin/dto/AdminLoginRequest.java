package com.changxing.admin.dto;

import lombok.Data;

@Data
public class AdminLoginRequest {
    private String phone;
    private String password;
}
