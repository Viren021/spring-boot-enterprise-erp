package com.example.erp_service_compliance.repository;

import com.example.erp_service_compliance.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // Spring magically writes the NoSQL query for this just based on the method name!
    List<AuditLog> findByTenantIdOrderByTimestampDesc(String tenantId);
}