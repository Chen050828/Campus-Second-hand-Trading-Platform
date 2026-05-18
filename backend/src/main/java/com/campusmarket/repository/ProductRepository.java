package com.campusmarket.repository;

import com.campusmarket.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByMerchantId(Long merchantId);
    List<Product> findByStatus(Product.ProductStatus status);
    List<Product> findByMerchantIdAndStatus(Long merchantId, Product.ProductStatus status);
    List<Product> findByCategoryIdAndStatus(Long categoryId, Product.ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND p.name LIKE %:keyword%")
    List<Product> searchByName(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' ORDER BY p.discountPrice ASC")
    List<Product> findAllApprovedOrderByPriceAsc();

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' ORDER BY p.discountPrice DESC")
    List<Product> findAllApprovedOrderByPriceDesc();

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' ORDER BY p.salesCount DESC")
    List<Product> findAllApprovedOrderBySalesDesc();

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' ORDER BY p.avgRating DESC")
    List<Product> findAllApprovedOrderByRatingDesc();

    List<Product> findByStatusAndNameContaining(Product.ProductStatus status, String name);
    void deleteByMerchantId(Long merchantId);
}
