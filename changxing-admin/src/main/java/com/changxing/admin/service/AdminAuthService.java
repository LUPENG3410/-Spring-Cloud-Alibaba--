package com.changxing.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.admin.dto.AdminLoginResponse;
import com.changxing.admin.entity.Admin;
import com.changxing.admin.mapper.AdminMapper;
import com.changxing.common.constant.Constants;
import com.changxing.common.exception.BusinessException;
import com.changxing.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminMapper adminMapper;
    private final StringRedisTemplate redisTemplate;

    public AdminLoginResponse login(String phone, String password) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getPhone, phone);
        Admin admin = adminMapper.selectOne(wrapper);

        if (admin == null) {
            throw new BusinessException(400, "管理员账号不存在");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(400, "账号已被禁用");
        }
        if (!admin.getPassword().equals(password)) {
            throw new BusinessException(400, "密码错误");
        }

        admin.setLastLogin(LocalDateTime.now());
        adminMapper.updateById(admin);

        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phone);
        claims.put("role", Constants.ROLE_ADMIN);
        claims.put("name", admin.getName());

        String accessToken = JwtUtils.generateToken(admin.getId().toString(), claims, Constants.ACCESS_TOKEN_EXPIRE);
        String refreshToken = JwtUtils.generateToken(admin.getId().toString(), claims, Constants.REFRESH_TOKEN_EXPIRE);

        redisTemplate.opsForValue().set(
                Constants.LOGIN_TOKEN_PREFIX + admin.getId(),
                accessToken,
                Constants.ACCESS_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        redisTemplate.opsForValue().set(
                Constants.REFRESH_TOKEN_PREFIX + admin.getId(),
                refreshToken,
                Constants.REFRESH_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        AdminLoginResponse response = new AdminLoginResponse();
        response.setId(admin.getId());
        response.setName(admin.getName());
        response.setPhone(admin.getPhone());
        response.setAvatar(admin.getAvatar());
        response.setRole(admin.getRole());
        response.setToken(accessToken);

        return response;
    }
}
