package com.example.erp_service_sales.repository;

import com.example.erp_service_sales.entity.SalesApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesApprovalPolicyRepository extends JpaRepository<SalesApprovalPolicy, Long> {
    List<SalesApprovalPolicy> findByTenantIdAndDocumentTypeAndActiveTrueOrderByMinAmount(
            String tenantId, String documentType);
}
