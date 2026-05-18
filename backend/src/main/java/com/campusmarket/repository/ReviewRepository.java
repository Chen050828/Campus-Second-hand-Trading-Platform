package com.campusmarket.repository;

import com.campusmarket.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
    List<Review> findByProductIdAndType(Long productId, Review.ReviewType type);
    List<Review> findByProduct_Merchant_IdAndType(Long merchantId, Review.ReviewType type);
    void deleteByUserId(Long userId);
}
