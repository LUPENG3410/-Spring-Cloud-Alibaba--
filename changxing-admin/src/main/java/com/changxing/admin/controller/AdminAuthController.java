package com.changxing.admin.controller;

import com.changxing.admin.dto.AdminLoginRequest;
import com.changxing.admin.dto.AdminLoginResponse;
import com.changxing.admin.service.AdminAuthService;
import com.changxing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthService.login(request.getPhone(), request.getPassword());
        return Result.success(response);
    }
}
