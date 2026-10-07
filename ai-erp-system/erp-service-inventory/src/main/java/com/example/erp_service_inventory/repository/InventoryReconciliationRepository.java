package com.example.erp_service_inventory.repository;

import com.example.erp_service_inventory.entity.InventoryReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryReconciliationRepository extends JpaRepository<InventoryReconciliation, Long> {
    List<InventoryReconciliation> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<InventoryReconciliation> findByTenantIdAndProductIdAndWarehouseIdAndLocationIdOrderByCreatedAtDesc(
            String tenantId, Long productId, Long warehouseId, Long locationId);
}
