package com.example.erp_service_inventory.repository;
import com.example.erp_service_inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByTenantIdAndProductIdOrderByOccurredAtDesc(String tenantId, Long productId);
    List<StockMovement> findByTenantIdAndProductIdAndWarehouseIdAndLocationId(
            String tenantId, Long productId, Long warehouseId, Long locationId);
    @Query("select m from StockMovement m where m.tenantId = :tenant and m.productId = :product " +
            "and m.warehouseId = :warehouse and (m.locationId = :location or " +
            "(:location is null and m.locationId is null)) order by m.occurredAt")
    List<StockMovement> findByTenantIdAndProductIdAndWarehouseIdAndLocationIdOrderByOccurredAt(
            @Param("tenant") String tenantId, @Param("product") Long productId,
            @Param("warehouse") Long warehouseId, @Param("location") Long locationId);
    java.util.Optional<StockMovement> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);
    java.util.Optional<StockMovement> findByIdAndTenantId(Long id, String tenantId);
}
