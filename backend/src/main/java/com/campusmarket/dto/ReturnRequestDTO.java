package com.campusmarket.dto;

import lombok.Data;

@Data
public class ReturnRequestDTO {
    private Long orderId;
    private String reason;
}
