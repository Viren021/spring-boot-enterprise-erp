package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.ApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalPolicyRepository extends JpaRepository<ApprovalPolicy, Long> {
    List<ApprovalPolicy> findByTenantIdAndDocumentTypeAndActiveTrueOrderByApprovalLevel(
            String tenantId, String documentType);
}
