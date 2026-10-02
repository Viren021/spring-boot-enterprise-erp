package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByIdAndTenantId(Long id, String tenantId);
    Optional<Vendor> findByVendorCodeAndTenantId(String vendorCode, String tenantId);
    List<Vendor> findByStatusAndTenantId(String status, String tenantId);
    List<Vendor> findByTenantId(String tenantId);
}
