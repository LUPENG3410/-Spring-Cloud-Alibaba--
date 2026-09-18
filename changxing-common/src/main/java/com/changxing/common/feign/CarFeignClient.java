package com.changxing.common.feign;

import com.changxing.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "changxing-car", path = "/cars")
public interface CarFeignClient {

    @GetMapping("/{trimId}")
    Result<Map<String, Object>> getCarById(@PathVariable("trimId") Long trimId);

    @GetMapping("/list")
    Result<Object> getCarList(@RequestParam(required = false) String keyword);

    @PutMapping("/{id}/status")
    Result<Void> updateCarStatus(@PathVariable("id") Long id, @RequestParam("status") String status);

    @PutMapping("/province")
    Result<Void> updateCarProvince(@RequestParam("trimId") Long trimId,
                                   @RequestParam("fromProvince") String fromProvince,
                                   @RequestParam("toProvince") String toProvince);
}
