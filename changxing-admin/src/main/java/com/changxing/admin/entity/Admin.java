package com.changxing.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admins")
public class Admin implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String phone;

    private String password;

    private String avatar;

    private String role;

    private LocalDateTime lastLogin;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
