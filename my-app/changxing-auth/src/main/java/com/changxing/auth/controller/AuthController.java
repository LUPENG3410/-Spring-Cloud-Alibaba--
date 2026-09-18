package com.changxing.auth.controller;

import com.changxing.auth.dto.AuthResponse;
import com.changxing.auth.dto.LoginRequest;
import com.changxing.auth.dto.RegisterRequest;
import com.changxing.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", response
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody @Valid RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", response
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("X-User-Id") Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "已退出登录"
        ));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "data", Map.of("userId", userId, "valid", true)
        ));
    }
}
