package com.campusmarket.service;

import com.campusmarket.dto.*;
import com.campusmarket.model.*;
import com.campusmarket.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.points.rate}")
    private int pointsRate;

    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId).orElse(null);
    }

    public List<WalletTransaction> getTransactions(Long userId) {
        return walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public ApiResponse<?> recharge(Long adminId, RechargeRequest req) {
        Wallet wallet = walletRepository.findByUserId(req.getUserId()).orElse(null);
        if (wallet == null) {
            return ApiResponse.error("用户钱包不存在");
        }

        wallet.setBalance(wallet.getBalance() + req.getAmount());
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUser(userRepository.findById(req.getUserId()).orElseThrow());
        tx.setWallet(wallet);
        tx.setAmount(req.getAmount());
        tx.setType(WalletTransaction.TransactionType.RECHARGE);
        tx.setDescription(req.getDescription() != null ? req.getDescription() : "管理员充值");
        walletTransactionRepository.save(tx);

        return ApiResponse.success("充值成功");
    }

    /**
     * 用户自行充值（模拟支付，直接加钱）
     */
    @Transactional
    public ApiResponse<?> selfRecharge(Long userId, Double amount) {
        if (amount == null || amount <= 0) {
            return ApiResponse.error("充值金额必须大于0");
        }
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) {
            return ApiResponse.error("钱包不存在");
        }
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUser(userRepository.findById(userId).orElseThrow());
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(WalletTransaction.TransactionType.RECHARGE);
        tx.setDescription("自行充值");
        walletTransactionRepository.save(tx);

        return ApiResponse.success("充值成功，当前余额: ¥" + String.format("%.2f", wallet.getBalance()));
    }

    @Transactional
    public int usePoints(Long userId, int points) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        int usable = Math.min(points, wallet.getPoints());
        double cashValue = (double) usable / pointsRate;
        wallet.setPoints(wallet.getPoints() - usable);
        wallet.setBalance(wallet.getBalance() + cashValue);
        walletRepository.save(wallet);

        if (usable > 0) {
            WalletTransaction tx = new WalletTransaction();
            tx.setUser(wallet.getUser());
            tx.setWallet(wallet);
            tx.setAmount(cashValue);
            tx.setType(WalletTransaction.TransactionType.POINTS_DEDUCTION);
            tx.setDescription("积分抵扣: " + usable + "积分 = ¥" + String.format("%.2f", cashValue));
            walletTransactionRepository.save(tx);
        }
        return usable;
    }
}
