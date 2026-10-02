package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.ProcurementAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcurementAuditEventRepository extends JpaRepository<ProcurementAuditEvent, Long> {
    List<ProcurementAuditEvent> findByTenantIdAndDocumentTypeAndDocumentIdOrderByOccurredAtAsc(
            String tenantId, String documentType, Long documentId);
}
