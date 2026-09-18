package com.changxing.auth.service;

import com.changxing.auth.dto.LoginResponse;
import com.changxing.common.constant.Constants;
import com.changxing.common.dto.Result;
import com.changxing.common.exception.BusinessException;
import com.changxing.common.feign.UserFeignClient;
import com.changxing.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserFeignClient userFeignClient;
    private final StringRedisTemplate redisTemplate;

    public LoginResponse login(String phone, String password) {
        Result<Map<String, Object>> result = userFeignClient.login(phone, password);
        if (result == null || result.getCode() != 200) {
            throw new BusinessException(400, result != null ? result.getMessage() : "登录失败");
        }

        Map<String, Object> userData = result.getData();
        Long userId = Long.valueOf(userData.get("id").toString());

        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phone);
        claims.put("role", Constants.ROLE_USER);
        claims.put("name", userData.get("name"));

        String accessToken = JwtUtils.generateToken(userId.toString(), claims, Constants.ACCESS_TOKEN_EXPIRE);
        String refreshToken = JwtUtils.generateToken(userId.toString(), claims, Constants.REFRESH_TOKEN_EXPIRE);

        redisTemplate.opsForValue().set(
                Constants.LOGIN_TOKEN_PREFIX + userId,
                accessToken,
                Constants.ACCESS_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        redisTemplate.opsForValue().set(
                Constants.REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                Constants.REFRESH_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(userId);
        response.setPhone(phone);
        response.setName((String) userData.get("name"));
        response.setAvatar((String) userData.get("avatar"));
        response.setMemberLevel((String) userData.get("memberLevel"));

        log.info("用户登录成功: phone={}", phone);
        return response;
    }

    public LoginResponse register(String phone, String password) {
        Result<Map<String, Object>> result = userFeignClient.register(phone, password);
        if (result == null || result.getCode() != 200) {
            throw new BusinessException(400, result != null ? result.getMessage() : "注册失败");
        }

        Map<String, Object> userData = result.getData();
        Long userId = Long.valueOf(userData.get("id").toString());

        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phone);
        claims.put("role", Constants.ROLE_USER);
        claims.put("name", userData.get("name"));

        String accessToken = JwtUtils.generateToken(userId.toString(), claims, Constants.ACCESS_TOKEN_EXPIRE);
        String refreshToken = JwtUtils.generateToken(userId.toString(), claims, Constants.REFRESH_TOKEN_EXPIRE);

        redisTemplate.opsForValue().set(
                Constants.LOGIN_TOKEN_PREFIX + userId,
                accessToken,
                Constants.ACCESS_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        redisTemplate.opsForValue().set(
                Constants.REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                Constants.REFRESH_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(userId);
        response.setPhone(phone);
        response.setName((String) userData.get("name"));
        response.setMemberLevel((String) userData.get("memberLevel"));

        log.info("用户注册成功: phone={}", phone);
        return response;
    }

    public void logout(Long userId) {
        redisTemplate.delete(Constants.LOGIN_TOKEN_PREFIX + userId);
        redisTemplate.delete(Constants.REFRESH_TOKEN_PREFIX + userId);
        log.info("用户登出成功: userId={}", userId);
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!JwtUtils.isTokenValid(refreshToken)) {
            throw new BusinessException(401, "Refresh Token已过期，请重新登录");
        }

        String userId = JwtUtils.getSubject(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get(Constants.REFRESH_TOKEN_PREFIX + userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(401, "Refresh Token无效");
        }

        Result<Map<String, Object>> result = userFeignClient.getUserById(Long.valueOf(userId));
        if (result == null || result.getCode() != 200) {
            throw new BusinessException(500, "获取用户信息失败");
        }

        Map<String, Object> userData = result.getData();
        String phone = (String) userData.get("phone");

        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phone);
        claims.put("role", Constants.ROLE_USER);
        claims.put("name", userData.get("name"));

        String newAccessToken = JwtUtils.generateToken(userId, claims, Constants.ACCESS_TOKEN_EXPIRE);
        String newRefreshToken = JwtUtils.generateToken(userId, claims, Constants.REFRESH_TOKEN_EXPIRE);

        redisTemplate.opsForValue().set(
                Constants.LOGIN_TOKEN_PREFIX + userId,
                newAccessToken,
                Constants.ACCESS_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        redisTemplate.opsForValue().set(
                Constants.REFRESH_TOKEN_PREFIX + userId,
                newRefreshToken,
                Constants.REFRESH_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS
        );

        LoginResponse response = new LoginResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setUserId(Long.valueOf(userId));
        response.setPhone(phone);
        response.setName((String) userData.get("name"));
        response.setAvatar((String) userData.get("avatar"));
        response.setMemberLevel((String) userData.get("memberLevel"));

        return response;
    }
}
