package com.campusmarket.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 32)
    private String orderNo;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double totalPrice;

    @Column
    private Double platformFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PAID;

    @Column(length = 255)
    private String meetLocation;

    @Column
    private LocalDateTime meetTime;

    @Column
    private LocalDateTime receiveTime; // when buyer confirmed receipt

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum OrderStatus {
        PAID, RECEIVED, RETURNING, RETURN_APPROVED, RETURN_REJECTED, RETURNED, COMPLETED
    }
}
