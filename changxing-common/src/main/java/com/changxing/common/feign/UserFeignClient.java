package com.changxing.common.feign;

import com.changxing.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "changxing-user", path = "/user")
public interface UserFeignClient {

    @GetMapping("/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long id);

    @GetMapping("/phone/{phone}")
    Result<Map<String, Object>> getUserByPhone(@PathVariable("phone") String phone);

    @PostMapping("/register")
    Result<Map<String, Object>> register(@RequestParam("phone") String phone, @RequestParam("password") String password);

    @PostMapping("/login")
    Result<Map<String, Object>> login(@RequestParam("phone") String phone, @RequestParam("password") String password);
}
