package com.campusmarket.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String city;
    private String gender;
    private String bankAccount;
    private String role; // USER or MERCHANT
    private String storeName;
    private String captcha;
    private String captchaKey;
}
