package com.example.erp_tenant_service.repository;


import com.example.erp_tenant_service.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    // Spring Boot automatically writes the SQL to find, save, and delete tenants!
    boolean existsByTenantId(String tenantId);
}
