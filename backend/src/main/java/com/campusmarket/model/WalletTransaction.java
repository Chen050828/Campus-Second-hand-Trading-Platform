package com.campusmarket.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(length = 255)
    private String description;

    @Column
    private Long relatedOrderId;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        RECHARGE, PURCHASE, REFUND, PLATFORM_FEE, POINTS_DEDUCTION
    }
}
