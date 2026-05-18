package com.campusmarket.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long orderId;
    private Long productId;
    private Integer rating;
    private String content;
    private String type; // PRODUCT or SERVICE
}
