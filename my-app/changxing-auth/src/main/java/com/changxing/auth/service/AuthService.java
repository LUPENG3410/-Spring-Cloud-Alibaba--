package com.changxing.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.auth.dto.AuthResponse;
import com.changxing.auth.dto.LoginRequest;
import com.changxing.auth.dto.RegisterRequest;
import com.changxing.auth.entity.User;
import com.changxing.auth.mapper.UserMapper;
import com.changxing.auth.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())
        );

        if (user == null) {
            throw new RuntimeException("账号不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getName(), user.getPhone(), "user");

        redisTemplate.opsForValue().set(
                "user:token:" + user.getId(),
                token,
                2, TimeUnit.HOURS
        );

        return AuthResponse.builder()
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .avatar(user.getAvatar())
                        .memberLevel(user.getMemberLevel())
                        .status(user.getStatus() == 1 ? "active" : "disabled")
                        .build())
                .token(token)
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())
        );
        if (count > 0) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAvatar("");
        user.setMemberLevel("普通会员");
        user.setStatus(1);
        userMapper.insert(user);

        String token = jwtUtils.generateToken(user.getId(), user.getName(), user.getPhone(), "user");

        redisTemplate.opsForValue().set(
                "user:token:" + user.getId(),
                token,
                2, TimeUnit.HOURS
        );

        return AuthResponse.builder()
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .avatar("")
                        .memberLevel("普通会员")
                        .status("active")
                        .build())
                .token(token)
                .build();
    }

    public void logout(Long userId) {
        redisTemplate.delete("user:token:" + userId);
    }
}
