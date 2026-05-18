package com.campusmarket.repository;

import com.campusmarket.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    CartItem findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserId(Long userId);
}
