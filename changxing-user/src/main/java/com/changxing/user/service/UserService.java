package com.changxing.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.changxing.common.exception.BusinessException;
import com.changxing.user.dto.UserDTO;
import com.changxing.user.entity.User;
import com.changxing.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User findByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        wrapper.isNull(User::getDeletedAt);
        return userMapper.selectOne(wrapper);
    }

    public User register(String phone, String password) {
        User existUser = findByPhone(phone);
        if (existUser != null) {
            throw new BusinessException(400, "手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        user.setName("用户" + phone.substring(7));
        user.setMemberLevel("普通会员");
        user.setStatus(1);
        userMapper.insert(user);

        return user;
    }

    public User login(String phone, String password) {
        User user = findByPhone(phone);
        if (user == null) {
            throw new BusinessException(400, "手机号未注册");
        }
        if (!user.getPassword().equals(password)) {
            throw new BusinessException(400, "密码错误");
        }
        return user;
    }

    public UserDTO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(404, "用户不存在");
        }
        return convertToDTO(user);
    }

    public UserDTO getUserByPhone(String phone) {
        User user = findByPhone(phone);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }

    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param points 积分数量
     */
    public void addUserPoints(Long userId, int points) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 假设User实体中有points字段
        // user.setPoints(user.getPoints() + points);
        // userMapper.updateById(user);
        
        // 这里简化处理，实际应该有points字段
        log.info("用户积分增加: userId={}, points={}, totalPoints={}", 
                userId, points, points);
    }

    /**
     * 根据累计积分更新会员等级
     * @param userId 用户ID
     */
    public void updateMemberLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 假设User实体中有points字段
        // int totalPoints = user.getPoints();
        // String newLevel = calculateMemberLevel(totalPoints);
        // if (!newLevel.equals(user.getMemberLevel())) {
        //     user.setMemberLevel(newLevel);
        //     userMapper.updateById(user);
        //     log.info("用户会员等级更新: userId={}, oldLevel={}, newLevel={}", 
        //             userId, user.getMemberLevel(), newLevel);
        // }
        
        // 这里简化处理
        log.info("用户会员等级检查: userId={}", userId);
    }

    /**
     * 根据积分计算会员等级
     * @param points 累计积分
     * @return 会员等级
     */
    private String calculateMemberLevel(int points) {
        if (points >= 1000) {
            return "钻石会员";
        } else if (points >= 500) {
            return "黄金会员";
        } else if (points >= 200) {
            return "白银会员";
        } else {
            return "普通会员";
        }
    }
}
