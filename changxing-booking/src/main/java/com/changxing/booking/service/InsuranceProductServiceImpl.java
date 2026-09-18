package com.changxing.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.changxing.booking.entity.InsuranceProduct;
import com.changxing.booking.mapper.InsuranceProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsuranceProductServiceImpl extends ServiceImpl<InsuranceProductMapper, InsuranceProduct> 
        implements InsuranceProductService {

    @Override
    public List<InsuranceProduct> listAllEnabled() {
        return list(new LambdaQueryWrapper<InsuranceProduct>()
                .eq(InsuranceProduct::getStatus, 1)
                .orderByAsc(InsuranceProduct::getSortOrder));
    }

    @Override
    public InsuranceProduct getByCode(String code) {
        return getOne(new LambdaQueryWrapper<InsuranceProduct>()
                .eq(InsuranceProduct::getCode, code));
    }
}
