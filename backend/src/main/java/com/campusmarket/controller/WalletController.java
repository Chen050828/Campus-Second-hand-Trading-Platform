package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.User;
import com.campusmarket.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public ApiResponse<?> getWallet(@AuthenticationPrincipal User user) {
        return ApiResponse.success(walletService.getWallet(user.getId()));
    }

    @GetMapping("/transactions")
    public ApiResponse<?> transactions(@AuthenticationPrincipal User user) {
        return ApiResponse.success(walletService.getTransactions(user.getId()));
    }

    @PostMapping("/points/use")
    public ApiResponse<?> usePoints(@AuthenticationPrincipal User user,
                                     @RequestBody Map<String, Integer> body) {
        int used = walletService.usePoints(user.getId(), body.get("points"));
        return ApiResponse.success("已使用 " + used + " 积分");
    }
}
