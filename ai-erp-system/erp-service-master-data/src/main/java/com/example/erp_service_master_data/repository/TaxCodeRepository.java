package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.TaxCode; import org.springframework.stereotype.Repository;
@Repository public interface TaxCodeRepository extends TenantRepository<TaxCode> {}
