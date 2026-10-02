package com.example.erp_service_inventory.repository;

import com.example.erp_service_inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    java.util.Optional<Product> findByIdAndTenantId(Long id, String tenantId);
    long countByTenantIdAndActiveTrue(String tenantId);
    long countByTenantIdAndActiveTrueAndStockQuantityLessThanEqual(String tenantId, Integer reorderLevel);
    java.util.List<Product> findByTenantIdAndActiveTrue(String tenantId);
}