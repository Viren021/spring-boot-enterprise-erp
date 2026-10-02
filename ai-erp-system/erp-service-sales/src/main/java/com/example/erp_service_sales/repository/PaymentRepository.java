package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.Payment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,Long> { List<Payment> findByTenantId(String tenantId); Optional<Payment> findByIdAndTenantId(Long id,String tenantId); List<Payment> findByInvoiceIdAndTenantId(Long invoiceId,String tenantId); }
