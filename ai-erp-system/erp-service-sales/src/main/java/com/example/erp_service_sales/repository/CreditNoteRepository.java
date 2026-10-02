package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.CreditNote; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CreditNoteRepository extends JpaRepository<CreditNote,Long> { List<CreditNote> findByTenantId(String tenantId); Optional<CreditNote> findByIdAndTenantId(Long id,String tenantId);  }
