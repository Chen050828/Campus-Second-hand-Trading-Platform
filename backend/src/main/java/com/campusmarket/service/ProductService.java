package com.campusmarket.service;

import com.campusmarket.dto.*;
import com.campusmarket.model.*;
import com.campusmarket.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public Product publishProduct(Long merchantId, ProductRequest req) {
        User merchant = userRepository.findById(merchantId).orElseThrow();
        Product product = new Product();
        product.setMerchant(merchant);
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setDiscountPrice(req.getDiscountPrice());
        product.setSize(req.getSize());
        product.setImages(req.getImages());
        product.setUsageNotes(req.getUsageNotes());
        product.setAllowBargain(req.getAllowBargain() != null ? req.getAllowBargain() : false);
        product.setQuantity(req.getQuantity());
        product.setCondition_(req.getCondition_());
        product.setStatus(Product.ProductStatus.PENDING);
        if (req.getCategoryId() != null) {
            categoryRepository.findById(req.getCategoryId()).ifPresent(product::setCategory);
        }
        return productRepository.save(product);
    }

    @Transactional
    public void delistProduct(Long merchantId, Long productId) {
        productRepository.findById(productId).ifPresent(p -> {
            if (p.getMerchant().getId().equals(merchantId)) {
                p.setStatus(Product.ProductStatus.DELISTED);
                productRepository.save(p);
            }
        });
    }

    public List<Product> getMerchantProducts(Long merchantId) {
        return productRepository.findByMerchantId(merchantId);
    }

    public List<Product> getMerchantProductsByStatus(Long merchantId, Product.ProductStatus status) {
        return productRepository.findByMerchantIdAndStatus(merchantId, status);
    }

    public List<Product> getApprovedProducts() {
        return productRepository.findByStatus(Product.ProductStatus.APPROVED);
    }

    public List<Product> getPendingProducts() {
        return productRepository.findByStatus(Product.ProductStatus.PENDING);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void adminDelistProduct(Long productId, String reason) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setStatus(Product.ProductStatus.DELISTED);
            p.setDelistReason(reason);
            productRepository.save(p);
        });
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void approveProduct(Long productId) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setStatus(Product.ProductStatus.APPROVED);
            productRepository.save(p);
        });
    }

    @Transactional
    public void rejectProduct(Long productId) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setStatus(Product.ProductStatus.REJECTED);
            productRepository.save(p);
        });
    }

    public List<Product> searchProducts(String keyword, String sortBy) {
        List<Product> results;
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = productRepository.searchByName(keyword);
        } else {
            results = productRepository.findByStatus(Product.ProductStatus.APPROVED);
        }

        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc":
                    results.sort(Comparator.comparing(Product::getDiscountPrice));
                    break;
                case "price_desc":
                    results.sort(Comparator.comparing(Product::getDiscountPrice).reversed());
                    break;
                case "sales":
                    results.sort(Comparator.comparing(Product::getSalesCount).reversed());
                    break;
                case "rating":
                    results.sort(Comparator.comparing(Product::getAvgRating, Comparator.nullsLast(Double::compareTo)).reversed());
                    break;
            }
        }
        return results;
    }

    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Map<String, Object> getProductDetail(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        List<Review> reviews = reviewRepository.findByProductId(productId);

        // Calculate average rating for the merchant
        long serviceReviewCount = reviewRepository
                .findByProduct_Merchant_IdAndType(product.getMerchant().getId(), Review.ReviewType.SERVICE).size();

        Map<String, Object> detail = new HashMap<>();
        detail.put("product", product);
        detail.put("reviews", reviews);
        detail.put("merchantServiceRating", product.getMerchant().getServiceRating());
        detail.put("merchantServiceReviewCount", serviceReviewCount);
        return detail;
    }

    @Transactional
    public void markSoldOut(Long productId) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setStatus(Product.ProductStatus.SOLD_OUT);
            productRepository.save(p);
        });
    }
}
