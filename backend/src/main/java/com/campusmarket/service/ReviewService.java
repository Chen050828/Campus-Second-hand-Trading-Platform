package com.campusmarket.service;

import com.campusmarket.dto.*;
import com.campusmarket.model.*;
import com.campusmarket.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public ApiResponse<?> createReview(Long userId, ReviewRequest req) {
        Product product = productRepository.findById(req.getProductId()).orElseThrow();
        Order order = req.getOrderId() != null ?
                orderRepository.findById(req.getOrderId()).orElse(null) : null;

        Review review = new Review();
        review.setUser(userRepository.findById(userId).orElseThrow());
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(req.getRating());
        review.setContent(req.getContent());

        Review.ReviewType type = Review.ReviewType.PRODUCT;
        if ("SERVICE".equals(req.getType())) {
            type = Review.ReviewType.SERVICE;
        }
        review.setType(type);
        reviewRepository.save(review);

        if (type == Review.ReviewType.PRODUCT) {
            // Update product average rating
            List<Review> productReviews = reviewRepository.findByProductIdAndType(product.getId(), Review.ReviewType.PRODUCT);
            double avg = productReviews.stream().mapToInt(Review::getRating).average().orElse(0);
            product.setAvgRating(Math.round(avg * 10.0) / 10.0);
            product.setReviewCount(productReviews.size());
            productRepository.save(product);
        } else {
            // Update merchant service rating
            User merchant = product.getMerchant();
            List<Review> serviceReviews = reviewRepository
                    .findByProduct_Merchant_IdAndType(merchant.getId(), Review.ReviewType.SERVICE);
            double avg = serviceReviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
            merchant.setServiceRating(Math.round(avg * 10.0) / 10.0);
            merchant.setServiceRatingCount(serviceReviews.size());
            userRepository.save(merchant);
        }

        return ApiResponse.success("评价成功");
    }

    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getMerchantServiceReviews(Long merchantId) {
        return reviewRepository.findByProduct_Merchant_IdAndType(merchantId, Review.ReviewType.SERVICE);
    }
}
