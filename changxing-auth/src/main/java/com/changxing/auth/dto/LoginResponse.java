package com.changxing.auth.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResponse implements Serializable {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private Long userId;
    private String phone;
    private String name;
    private String avatar;
    private String memberLevel;

    public LoginResponse() {
        this.tokenType = "Bearer";
        this.expiresIn = 7200L;
    }
}
