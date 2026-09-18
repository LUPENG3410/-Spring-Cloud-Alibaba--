package com.changxing.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stores")
public class Store implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String province;

    private String city;

    private String address;

    private String phone;

    private BigDecimal lat;

    private BigDecimal lng;

    private String hours;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
