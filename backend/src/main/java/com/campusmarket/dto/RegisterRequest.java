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
    private String businessLicenseImg; // 营业执照图片URL（商家注册）
    private String idCardImg;          // 身份证图片URL（商家注册）
    private String captcha;
    private String captchaKey;
}
