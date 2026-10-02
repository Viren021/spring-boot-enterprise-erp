package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TenantRepository<T extends TenantEntity> extends JpaRepository<T, UUID> {
 List<T> findAllByTenantIdOrderByCreatedAtDesc(String tenantId);
 Optional<T> findByIdAndTenantId(UUID id, String tenantId);
}
