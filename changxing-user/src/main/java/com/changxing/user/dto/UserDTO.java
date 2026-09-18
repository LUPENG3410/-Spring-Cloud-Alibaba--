package com.changxing.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO implements Serializable {

    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private String memberLevel;
    private Integer status;
}
