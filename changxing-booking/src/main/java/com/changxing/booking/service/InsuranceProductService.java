package com.changxing.booking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.changxing.booking.entity.InsuranceProduct;

import java.util.List;

public interface InsuranceProductService extends IService<InsuranceProduct> {

    List<InsuranceProduct> listAllEnabled();

    InsuranceProduct getByCode(String code);
}
