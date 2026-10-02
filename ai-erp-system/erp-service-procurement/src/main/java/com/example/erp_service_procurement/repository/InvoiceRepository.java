package com.example.erp_service_procurement.repository;

import com.example.erp_service_procurement.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByIdAndTenantId(Long id, String tenantId);
    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, String tenantId);
    List<Invoice> findByVendorIdAndTenantId(Long vendorId, String tenantId);
    List<Invoice> findByStatusAndTenantId(String status, String tenantId);
    List<Invoice> findByTenantId(String tenantId);
}
