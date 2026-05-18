package com.campusmarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String city;

    @Column(length = 10)
    private String gender;

    @Column(length = 16)
    private String bankAccount;

    @Column(length = 255)
    private String businessLicenseImg;

    @Column(length = 255)
    private String idCardImg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status = UserStatus.PENDING;

    @Column
    private Integer merchantLevel; // 1-5, only for merchants

    @Column(length = 100)
    private String storeName; // merchant store name

    @Column
    private Double serviceRating = 5.0; // merchant service rating

    @Column
    private Integer serviceRatingCount = 0;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum UserRole {
        USER, MERCHANT, ADMIN
    }

    public enum UserStatus {
        PENDING, APPROVED, REJECTED
    }
}
