package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/captcha")
    public ApiResponse<?> getCaptcha() {
        String key = UUID.randomUUID().toString();
        String code = userService.generateCaptcha(key);
        return ApiResponse.success(Map.of("captchaKey", key, "captchaCode", code));
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest req) {
        return userService.register(req);
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest req) {
        return userService.login(req);
    }
}
