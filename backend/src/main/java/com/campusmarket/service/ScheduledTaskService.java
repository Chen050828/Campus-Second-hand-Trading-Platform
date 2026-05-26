package com.campusmarket.service;

import com.campusmarket.model.*;
import com.campusmarket.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：处理超时自动确认收货。
 * 买家下单后超过7天未确认收货，系统自动完成交易并将货款转给卖家。
 */
@Service
public class ScheduledTaskService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SystemAccountRepository systemAccountRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Value("${app.merchant.auto-confirm-days}")
    private int autoConfirmDays;

    // 每小时执行一次，扫描所有PAID状态且超过7天的订单自动确认
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void autoConfirmOrders() {
        List<Order> paidOrders = orderRepository.findByStatus(Order.OrderStatus.PAID);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(autoConfirmDays);

        for (Order order : paidOrders) {
            if (order.getCreatedAt().isBefore(cutoff)) {
                confirmOrder(order);
            }
        }

        // RECEIVED 超过24小时退货窗口，自动转为 COMPLETED
        List<Order> receivedOrders = orderRepository.findByStatus(Order.OrderStatus.RECEIVED);
        LocalDateTime returnCutoff = LocalDateTime.now().minusHours(24);

        for (Order order : receivedOrders) {
            if (order.getReceiveTime() != null && order.getReceiveTime().isBefore(returnCutoff)) {
                order.setStatus(Order.OrderStatus.COMPLETED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }
        }
    }

    // 单个订单自动确认：中间账户转款给卖家，订单标记完成
    private void confirmOrder(Order order) {
        order.setStatus(Order.OrderStatus.RECEIVED);
        order.setReceiveTime(LocalDateTime.now());

        SystemAccount sysAccount = getSystemAccount();
        double totalPrice = order.getTotalPrice();
        double fee = order.getPlatformFee() != null ? order.getPlatformFee() : 0;
        double merchantAmount = totalPrice - fee;

        if (sysAccount.getBalance() >= totalPrice) {
            sysAccount.setBalance(sysAccount.getBalance() - totalPrice);
            sysAccount.setTotalFee(sysAccount.getTotalFee() + fee);
            systemAccountRepository.save(sysAccount);

            Wallet merchantWallet = walletRepository
                    .findByUserId(order.getProduct().getMerchant().getId()).orElse(null);
            if (merchantWallet != null) {
                merchantWallet.setBalance(merchantWallet.getBalance() + merchantAmount);
                walletRepository.save(merchantWallet);

                WalletTransaction tx = new WalletTransaction();
                tx.setUser(order.getProduct().getMerchant());
                tx.setWallet(merchantWallet);
                tx.setAmount(merchantAmount);
                tx.setType(WalletTransaction.TransactionType.REFUND);
                tx.setDescription("系统自动确认收货: " + order.getProduct().getName());
                walletTransactionRepository.save(tx);
            }
        }

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private SystemAccount getSystemAccount() {
        List<SystemAccount> accounts = systemAccountRepository.findAll();
        if (accounts.isEmpty()) {
            SystemAccount acc = new SystemAccount();
            return systemAccountRepository.save(acc);
        }
        return accounts.get(0);
    }
}
