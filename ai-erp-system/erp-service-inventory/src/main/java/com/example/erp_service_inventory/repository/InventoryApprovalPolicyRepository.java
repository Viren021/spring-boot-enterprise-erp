package com.example.erp_service_inventory.repository;

import com.example.erp_service_inventory.entity.InventoryApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryApprovalPolicyRepository extends JpaRepository<InventoryApprovalPolicy, Long> {
    List<InventoryApprovalPolicy> findByTenantIdAndOperationAndActiveTrue(String tenantId, String operation);
}
