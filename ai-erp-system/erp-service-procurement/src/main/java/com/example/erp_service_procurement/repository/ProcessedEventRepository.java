package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByTenantIdAndEventId(String tenantId, String eventId);
}
