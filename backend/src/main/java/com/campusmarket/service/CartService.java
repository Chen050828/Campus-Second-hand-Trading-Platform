package com.campusmarket.service;

import com.campusmarket.model.*;
import com.campusmarket.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public void addToCart(Long userId, Long productId, Integer quantity) {
        CartItem existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + (quantity != null ? quantity : 1));
            cartItemRepository.save(existing);
        } else {
            Product product = productRepository.findById(productId).orElseThrow();
            CartItem item = new CartItem();
            item.setUser(userRepository.findById(userId).orElseThrow());
            item.setProduct(product);
            item.setQuantity(quantity != null ? quantity : 1);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (item != null && quantity > 0) {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
