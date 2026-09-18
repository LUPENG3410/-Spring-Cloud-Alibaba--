package com.changxing.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "账号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
