package com.example.erp_service_inventory.repository;

import com.example.erp_service_inventory.entity.InventoryCostLayer;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface InventoryCostLayerRepository extends JpaRepository<InventoryCostLayer, Long> {
    @Query("select l from InventoryCostLayer l where l.tenantId = :tenant and l.productId = :product " +
            "and l.warehouseId = :warehouse and (l.locationId = :location or " +
            "(:location is null and l.locationId is null)) and l.quantityRemaining > :remaining order by l.createdAt")
    List<InventoryCostLayer> findByTenantIdAndProductIdAndWarehouseIdAndLocationIdAndQuantityRemainingGreaterThanOrderByCreatedAt(
            @Param("tenant") String tenantId, @Param("product") Long productId,
            @Param("warehouse") Long warehouseId, @Param("location") Long locationId,
            @Param("remaining") BigDecimal remaining);
}
