package com.campusmarket.dto;

import lombok.Data;

@Data
public class RechargeRequest {
    private Long userId;
    private Double amount;
    private String description;
}
