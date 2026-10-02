package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByIdAndTenantId(Long id, String tenantId);
    List<Receipt> findByPurchaseOrderId(Long poId);
    List<Receipt> findByTenantId(String tenantId);
}
