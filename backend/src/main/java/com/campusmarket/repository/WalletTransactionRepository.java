package com.campusmarket.repository;

import com.campusmarket.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    void deleteByWalletId(Long walletId);
}
