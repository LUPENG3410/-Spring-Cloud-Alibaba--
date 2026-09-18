package com.changxing.user.controller;

import com.changxing.common.dto.Result;
import com.changxing.user.dto.UserDTO;
import com.changxing.user.entity.User;
import com.changxing.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/phone/{phone}")
    public Result<UserDTO> getUserByPhone(@PathVariable String phone) {
        return Result.success(userService.getUserByPhone(phone));
    }

    @PostMapping("/register")
    public Result<UserDTO> register(@RequestParam String phone, @RequestParam String password) {
        User user = userService.register(phone, password);
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setPhone(user.getPhone());
        dto.setName(user.getName());
        dto.setMemberLevel(user.getMemberLevel());
        dto.setStatus(user.getStatus());
        return Result.success(dto);
    }

    @PostMapping("/login")
    public Result<UserDTO> login(@RequestParam String phone, @RequestParam String password) {
        User user = userService.login(phone, password);
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setPhone(user.getPhone());
        dto.setName(user.getName());
        dto.setAvatar(user.getAvatar());
        dto.setMemberLevel(user.getMemberLevel());
        dto.setStatus(user.getStatus());
        return Result.success(dto);
    }
}
