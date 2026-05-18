package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.*;
import com.campusmarket.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String sortBy) {
        return ApiResponse.success(productService.searchProducts(keyword, sortBy));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductDetail(id));
    }

    @GetMapping("/{id}/reviews")
    public ApiResponse<?> reviews(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductReviews(id));
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<?> byCategory(@PathVariable Long categoryId) {
        return ApiResponse.success(productService.getApprovedProducts()
                .stream().filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                .toList());
    }

    // Merchant endpoints

    @PostMapping("/merchant/publish")
    public ApiResponse<?> publish(@AuthenticationPrincipal User merchant,
                                   @RequestBody ProductRequest req) {
        Product p = productService.publishProduct(merchant.getId(), req);
        return ApiResponse.success("发布成功，等待审核", p);
    }

    @PutMapping("/merchant/delist/{productId}")
    public ApiResponse<?> delist(@AuthenticationPrincipal User merchant,
                                  @PathVariable Long productId) {
        productService.delistProduct(merchant.getId(), productId);
        return ApiResponse.success("下架成功");
    }

    @GetMapping("/merchant/my")
    public ApiResponse<?> myProducts(@AuthenticationPrincipal User merchant,
                                      @RequestParam(required = false) String status) {
        if (status != null) {
            return ApiResponse.success(productService.getMerchantProductsByStatus(
                    merchant.getId(), Product.ProductStatus.valueOf(status)));
        }
        return ApiResponse.success(productService.getMerchantProducts(merchant.getId()));
    }
}
