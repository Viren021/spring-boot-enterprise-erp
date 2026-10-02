package com.example.erp_service_inventory.repository;
import com.example.erp_service_inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByTenantIdAndWarehouseId(String tenantId, Long warehouseId);
}
