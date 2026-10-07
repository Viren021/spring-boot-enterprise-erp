package com.example.erp_service_inventory.repository;

import com.example.erp_service_inventory.entity.InventoryCostLayer;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface InventoryCostLayerRepository extends JpaRepository<InventoryCostLayer, Long> {
    List<InventoryCostLayer> findByTenantIdAndProductIdAndWarehouseIdAndLocationIdAndQuantityRemainingGreaterThanOrderByCreatedAt(
            String tenantId, Long productId, Long warehouseId, Long locationId, BigDecimal remaining);
}
