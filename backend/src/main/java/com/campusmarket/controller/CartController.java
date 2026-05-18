package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.User;
import com.campusmarket.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ApiResponse<?> list(@AuthenticationPrincipal User user) {
        return ApiResponse.success(cartService.getCartItems(user.getId()));
    }

    @PostMapping("/add")
    public ApiResponse<?> add(@AuthenticationPrincipal User user,
                               @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer quantity = body.containsKey("quantity") ?
                Integer.valueOf(body.get("quantity").toString()) : 1;
        cartService.addToCart(user.getId(), productId, quantity);
        return ApiResponse.success("已加入购物车");
    }

    @DeleteMapping("/remove/{productId}")
    public ApiResponse<?> remove(@AuthenticationPrincipal User user,
                                  @PathVariable Long productId) {
        cartService.removeFromCart(user.getId(), productId);
        return ApiResponse.success("已移除");
    }

    @PutMapping("/update/{productId}")
    public ApiResponse<?> updateQuantity(@AuthenticationPrincipal User user,
                                          @PathVariable Long productId,
                                          @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(user.getId(), productId, body.get("quantity"));
        return ApiResponse.success("已更新");
    }

    @DeleteMapping("/clear")
    public ApiResponse<?> clear(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
        return ApiResponse.success("购物车已清空");
    }
}
