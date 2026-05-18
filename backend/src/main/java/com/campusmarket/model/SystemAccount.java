package com.campusmarket.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "system_account")
@Data
public class SystemAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double balance = 0.0; // intermediate account for pending funds

    @Column(nullable = false)
    private Double totalFee = 0.0; // accumulated platform fees
}
