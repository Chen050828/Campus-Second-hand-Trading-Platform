package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.User;
import com.campusmarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ApiResponse<?> getProfile(@AuthenticationPrincipal User user) {
        return ApiResponse.success(userService.getById(user.getId()));
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@AuthenticationPrincipal User user,
                                         @RequestBody User updates) {
        userService.updateUser(user.getId(), updates);
        return ApiResponse.success("更新成功");
    }

    @GetMapping("/info/{id}")
    public ApiResponse<?> getUserInfo(@PathVariable Long id) {
        User u = userService.getById(id);
        if (u == null) return ApiResponse.error("用户不存在");
        // Hide sensitive info
        u.setPassword(null);
        u.setBankAccount(null);
        return ApiResponse.success(u);
    }
}
