package com.changxing.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("insurance_products")
public class InsuranceProduct implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String description;

    private BigDecimal dailyPrice;

    private BigDecimal totalPrice;

    private BigDecimal vehicleLossDeductible;

    private BigDecimal vehicleLossCoverageRate;

    private Boolean tireLossCovered;

    private BigDecimal thirdPartyLimit;

    private Boolean thirdPartyMedicalCovered;

    private BigDecimal thirdPartyMedicalLimit;

    private BigDecimal driverLossLimit;

    private Boolean stopFeeCovered;

    private BigDecimal singleAccidentNoDocLimit;

    private Integer status;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
