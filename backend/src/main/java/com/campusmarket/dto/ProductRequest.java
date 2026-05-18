package com.campusmarket.dto;

import lombok.Data;

@Data
public class ProductRequest {
    private Long categoryId;
    private String name;
    private String description;
    private Double originalPrice;
    private Double discountPrice;
    private String size;
    private String images;
    private String usageNotes;
    private Boolean allowBargain;
    private Integer quantity;
    private String condition_;
}
