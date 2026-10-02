package com.example.erp_service_finance.repository;
import com.example.erp_service_finance.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, UUID> {
    List<FiscalPeriod> findByTenantIdOrderByStartDate(String tenantId);
}
