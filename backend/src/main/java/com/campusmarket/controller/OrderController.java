package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.User;
import com.campusmarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ApiResponse<?> create(@AuthenticationPrincipal User user,
                                  @RequestBody OrderRequest req) {
        return orderService.createOrder(user.getId(), req);
    }

    @GetMapping
    public ApiResponse<?> myOrders(@AuthenticationPrincipal User user) {
        return ApiResponse.success(orderService.getUserOrders(user.getId()));
    }

    @PutMapping("/{orderId}/receive")
    public ApiResponse<?> confirmReceive(@AuthenticationPrincipal User user,
                                          @PathVariable Long orderId) {
        return orderService.confirmReceive(user.getId(), orderId);
    }

    @PostMapping("/return")
    public ApiResponse<?> requestReturn(@AuthenticationPrincipal User user,
                                         @RequestBody ReturnRequestDTO req) {
        return orderService.requestReturn(user.getId(), req);
    }

    @GetMapping("/returns")
    public ApiResponse<?> myReturns(@AuthenticationPrincipal User user) {
        return ApiResponse.success(orderService.getUserReturns(user.getId()));
    }

    // Merchant endpoints
    @GetMapping("/merchant")
    public ApiResponse<?> merchantOrders(@AuthenticationPrincipal User merchant) {
        return ApiResponse.success(orderService.getMerchantOrders(merchant.getId()));
    }

    @GetMapping("/merchant/returns")
    public ApiResponse<?> merchantReturns(@AuthenticationPrincipal User merchant) {
        return ApiResponse.success(orderService.getMerchantReturns(merchant.getId()));
    }

    @PutMapping("/merchant/return/{returnId}/approve")
    public ApiResponse<?> approveReturn(@AuthenticationPrincipal User merchant,
                                         @PathVariable Long returnId) {
        return orderService.approveReturn(merchant.getId(), returnId);
    }

    @PutMapping("/merchant/return/{returnId}/reject")
    public ApiResponse<?> rejectReturn(@AuthenticationPrincipal User merchant,
                                        @PathVariable Long returnId,
                                        @RequestBody String reply) {
        return orderService.rejectReturn(merchant.getId(), returnId, reply);
    }
}
