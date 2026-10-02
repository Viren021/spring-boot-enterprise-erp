package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.Invoice; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice,Long> { List<Invoice> findByTenantId(String tenantId); Optional<Invoice> findByIdAndTenantId(Long id,String tenantId); Optional<Invoice> findByInvoiceNumberAndTenantId(String number,String tenantId); }
