package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.Currency; import org.springframework.stereotype.Repository;
@Repository public interface CurrencyRepository extends TenantRepository<Currency> {}
