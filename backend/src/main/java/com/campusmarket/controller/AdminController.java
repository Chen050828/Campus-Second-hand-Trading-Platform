package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.*;
import com.campusmarket.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private WalletService walletService;

    // User management

    @GetMapping("/users")
    public ApiResponse<?> listUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/users/pending")
    public ApiResponse<?> pendingUsers() {
        return ApiResponse.success(userService.getPendingUsers());
    }

    @PutMapping("/users/{userId}/approve")
    public ApiResponse<?> approveUser(@PathVariable Long userId) {
        userService.approveUser(userId);
        return ApiResponse.success("审核通过");
    }

    @PutMapping("/users/{userId}/reject")
    public ApiResponse<?> rejectUser(@PathVariable Long userId) {
        userService.rejectUser(userId);
        return ApiResponse.success("已拒绝");
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.success("已删除");
    }

    @PutMapping("/users/{userId}/level")
    public ApiResponse<?> updateMerchantLevel(@PathVariable Long userId,
                                               @RequestBody Map<String, Integer> body) {
        userService.updateMerchantLevel(userId, body.get("level"));
        return ApiResponse.success("商家等级已更新");
    }

    @GetMapping("/merchants")
    public ApiResponse<?> listMerchants() {
        return ApiResponse.success(userService.getMerchants());
    }

    // Product review management

    @GetMapping("/products/pending")
    public ApiResponse<?> pendingProducts() {
        return ApiResponse.success(productService.getPendingProducts());
    }

    @PutMapping("/products/{productId}/approve")
    public ApiResponse<?> approveProduct(@PathVariable Long productId) {
        productService.approveProduct(productId);
        return ApiResponse.success("商品审核通过");
    }

    @PutMapping("/products/{productId}/reject")
    public ApiResponse<?> rejectProduct(@PathVariable Long productId) {
        productService.rejectProduct(productId);
        return ApiResponse.success("商品已拒绝");
    }

    @GetMapping("/products/all")
    public ApiResponse<?> allProducts() {
        return ApiResponse.success(productService.getAllProducts());
    }

    @PutMapping("/products/{productId}/delist")
    public ApiResponse<?> delistProduct(@PathVariable Long productId,
                                         @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "管理员下架");
        productService.adminDelistProduct(productId, reason);
        return ApiResponse.success("商品已下架");
    }

    // Wallet recharge

    @PostMapping("/wallet/recharge")
    public ApiResponse<?> recharge(@RequestBody RechargeRequest req) {
        return walletService.recharge(null, req);
    }
}
