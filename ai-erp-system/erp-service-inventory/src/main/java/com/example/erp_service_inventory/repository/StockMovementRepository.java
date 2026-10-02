package com.example.erp_service_inventory.repository;
import com.example.erp_service_inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByTenantIdAndProductIdOrderByOccurredAtDesc(String tenantId, Long productId);
}
