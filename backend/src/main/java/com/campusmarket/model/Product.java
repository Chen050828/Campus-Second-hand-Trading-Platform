package com.campusmarket.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double originalPrice;

    @Column(nullable = false)
    private Double discountPrice;

    @Column(length = 100)
    private String size;

    @Column(columnDefinition = "TEXT")
    private String images; // JSON array of image paths

    @Column(columnDefinition = "TEXT")
    private String usageNotes;

    @Column
    private Boolean allowBargain = false;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 20)
    private String condition_; // NEW, LIKE_NEW, EIGHTY, SEVENTY, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ProductStatus status = ProductStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String delistReason;

    @Column
    private Integer salesCount = 0;

    @Column
    private Double avgRating = 0.0;

    @Column
    private Integer reviewCount = 0;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum ProductStatus {
        PENDING, APPROVED, REJECTED, SOLD_OUT, DELISTED
    }
}
