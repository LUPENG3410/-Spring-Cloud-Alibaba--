package com.changxing.booking.controller;

import com.changxing.booking.entity.InsuranceProduct;
import com.changxing.booking.service.InsuranceProductService;
import com.changxing.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "保险产品接口")
@RestController
@RequestMapping("/insurance-products")
@RequiredArgsConstructor
public class InsuranceProductController {

    private final InsuranceProductService insuranceProductService;

    @Operation(summary = "获取所有启用的保险产品")
    @GetMapping
    public Result<List<InsuranceProduct>> listAll() {
        return Result.success(insuranceProductService.listAllEnabled());
    }
}
