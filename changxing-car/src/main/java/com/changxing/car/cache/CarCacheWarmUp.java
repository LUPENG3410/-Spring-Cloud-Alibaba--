package com.changxing.car.cache;

import com.changxing.car.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarCacheWarmUp implements ApplicationRunner {

    private final CarService carService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            carService.getHotTrims();
            carService.getHasStockCarList(null, null, null);
            log.info("车辆缓存预热完成");
        } catch (Exception e) {
            log.warn("车辆缓存预热失败: {}", e.getMessage());
        }
    }
}