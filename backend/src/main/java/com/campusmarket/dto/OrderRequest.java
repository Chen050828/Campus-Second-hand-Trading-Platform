package com.campusmarket.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {
    private String meetLocation;
    private LocalDateTime meetTime;
    private List<CartOrderItem> items;

    @Data
    public static class CartOrderItem {
        private Long productId;
        private Integer quantity;
    }
}
