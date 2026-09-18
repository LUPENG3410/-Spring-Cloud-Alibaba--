package com.changxing.admin.dto;

import lombok.Data;

@Data
public class AdminLoginResponse {
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private String role;
    private String token;
}
