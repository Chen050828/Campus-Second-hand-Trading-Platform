package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.User;
import com.campusmarket.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ApiResponse<?> create(@AuthenticationPrincipal User user,
                                  @RequestBody ReviewRequest req) {
        return reviewService.createReview(user.getId(), req);
    }

    @GetMapping("/merchant/{merchantId}/service")
    public ApiResponse<?> merchantServiceReviews(@PathVariable Long merchantId) {
        return ApiResponse.success(reviewService.getMerchantServiceReviews(merchantId));
    }
}
