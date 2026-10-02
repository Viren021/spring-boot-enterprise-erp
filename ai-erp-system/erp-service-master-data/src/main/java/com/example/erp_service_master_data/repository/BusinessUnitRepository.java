package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.BusinessUnit; import org.springframework.stereotype.Repository;
@Repository public interface BusinessUnitRepository extends TenantRepository<BusinessUnit> {}
