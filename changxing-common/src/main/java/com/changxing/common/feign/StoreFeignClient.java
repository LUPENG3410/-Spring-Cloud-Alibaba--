package com.changxing.common.feign;

import com.changxing.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "changxing-store", path = "/store")
public interface StoreFeignClient {

    @GetMapping("/{id}")
    Result<Map<String, Object>> getStoreById(@PathVariable("id") Long id);

    @GetMapping("/list")
    Result<Object> getStoreList(@RequestParam(required = false) String city);
}
