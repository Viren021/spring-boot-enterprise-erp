package com.example.erp_service_inventory.repository;
import com.example.erp_service_inventory.entity.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {
    java.util.List<StockBalance> findByTenantId(String tenantId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockBalance> findByTenantIdAndProductIdAndWarehouseIdAndLocationId(String tenantId, Long productId, Long warehouseId, Long locationId);
    List<StockBalance> findByTenantIdAndProductId(String tenantId, Long productId);
}
