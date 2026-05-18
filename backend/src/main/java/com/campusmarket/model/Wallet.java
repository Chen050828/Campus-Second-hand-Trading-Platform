package com.campusmarket.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Double balance = 0.0;

    @Column(nullable = false)
    private Integer points = 0;

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();
}
