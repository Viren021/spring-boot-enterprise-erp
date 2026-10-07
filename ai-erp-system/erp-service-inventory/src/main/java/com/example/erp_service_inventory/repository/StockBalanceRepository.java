package com.example.erp_service_inventory.repository;
import com.example.erp_service_inventory.entity.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    java.util.List<StockBalance> findByTenantId(String tenantId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from StockBalance b where b.tenantId = :tenant and b.productId = :product " +
            "and b.warehouseId = :warehouse and (b.locationId = :location or " +
            "(:location is null and b.locationId is null))")
    Optional<StockBalance> findByTenantIdAndProductIdAndWarehouseIdAndLocationId(
            @Param("tenant") String tenantId, @Param("product") Long productId,
            @Param("warehouse") Long warehouseId, @Param("location") Long locationId);
    List<StockBalance> findByTenantIdAndProductId(String tenantId, Long productId);
}
