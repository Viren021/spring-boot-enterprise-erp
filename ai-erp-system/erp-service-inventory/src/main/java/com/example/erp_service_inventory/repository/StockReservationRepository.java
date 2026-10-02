package com.example.erp_service_inventory.repository;
import com.example.erp_service_inventory.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findByTenantIdAndStatus(String tenantId, String status);
}
