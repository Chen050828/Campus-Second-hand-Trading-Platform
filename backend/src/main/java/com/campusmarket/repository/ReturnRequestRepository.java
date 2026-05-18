package com.campusmarket.repository;

import com.campusmarket.model.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findByOrderId(Long orderId);
    List<ReturnRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ReturnRequest> findByOrder_Product_Merchant_IdAndStatus(Long merchantId, ReturnRequest.ReturnStatus status);
    void deleteByUserId(Long userId);
}
