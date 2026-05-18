package com.campusmarket.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String captcha;
    private String captchaKey;
}
