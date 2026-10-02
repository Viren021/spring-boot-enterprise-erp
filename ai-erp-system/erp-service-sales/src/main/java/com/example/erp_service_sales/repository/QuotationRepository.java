package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.Quotation; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface QuotationRepository extends JpaRepository<Quotation,Long> { List<Quotation> findByTenantId(String tenantId); Optional<Quotation> findByIdAndTenantId(Long id,String tenantId);  }
