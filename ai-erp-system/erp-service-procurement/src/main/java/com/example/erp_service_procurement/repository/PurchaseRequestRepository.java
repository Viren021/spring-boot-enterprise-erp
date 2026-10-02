package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    Optional<PurchaseRequest> findByIdAndTenantId(Long id, String tenantId);
    Optional<PurchaseRequest> findByPrNumberAndTenantId(String prNumber, String tenantId);
    List<PurchaseRequest> findByStatusAndTenantId(String status, String tenantId);
    List<PurchaseRequest> findByTenantId(String tenantId);
}
