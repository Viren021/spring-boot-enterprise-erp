package com.example.erp_service_sales.service;

import com.example.erp_service_sales.config.TenantContext;
import com.example.erp_service_sales.entity.SalesApprovalPolicy;
import com.example.erp_service_sales.repository.SalesApprovalPolicyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalesApprovalPolicyService {
    private final SalesApprovalPolicyRepository repository;
    public SalesApprovalPolicyService(SalesApprovalPolicyRepository repository) { this.repository = repository; }

    public SalesApprovalPolicy save(SalesApprovalPolicy policy) {
        policy.setId(null);
        policy.setTenantId(TenantContext.required());
        if (policy.getDocumentType() == null || policy.getDocumentType().isBlank())
            throw new IllegalArgumentException("documentType is required");
        if (policy.getMinAmount() == null || policy.getMinAmount().signum() < 0)
            throw new IllegalArgumentException("minAmount cannot be negative");
        if (policy.getMaxAmount() != null && policy.getMaxAmount().compareTo(policy.getMinAmount()) < 0)
            throw new IllegalArgumentException("maxAmount cannot be below minAmount");
        return repository.save(policy);
    }

    public List<SalesApprovalPolicy> list() {
        return repository.findByTenantIdAndDocumentTypeAndActiveTrueOrderByMinAmount(
                TenantContext.required(), "SALES_ORDER");
    }

    public boolean requiresApproval(BigDecimal amount) {
        return list().stream().anyMatch(p -> p.isMakerChecker()
                && amount.compareTo(p.getMinAmount()) >= 0
                && (p.getMaxAmount() == null || amount.compareTo(p.getMaxAmount()) <= 0));
    }
}
