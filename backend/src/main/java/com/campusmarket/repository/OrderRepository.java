package com.campusmarket.repository;

import com.campusmarket.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByProduct_Merchant_Id(Long merchantId);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
    List<Order> findByProduct_Merchant_IdAndStatus(Long merchantId, Order.OrderStatus status);
    void deleteByUserId(Long userId);
}
