package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.QuotationLine; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface QuotationLineRepository extends JpaRepository<QuotationLine,Long> { List<QuotationLine> findByTenantId(String tenantId); Optional<QuotationLine> findByIdAndTenantId(Long id,String tenantId); List<QuotationLine> findByQuotationIdAndTenantId(Long quotationId,String tenantId); }
