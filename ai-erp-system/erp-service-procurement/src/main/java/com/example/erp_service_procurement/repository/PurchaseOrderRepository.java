package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByIdAndTenantId(Long id, String tenantId);
    Optional<PurchaseOrder> findByPoNumberAndTenantId(String poNumber, String tenantId);
    List<PurchaseOrder> findByVendorIdAndTenantId(Long vendorId, String tenantId);
    List<PurchaseOrder> findByStatusAndTenantId(String status, String tenantId);
    List<PurchaseOrder> findByTenantId(String tenantId);
}
