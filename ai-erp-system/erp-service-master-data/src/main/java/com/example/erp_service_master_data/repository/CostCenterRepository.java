package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.CostCenter; import org.springframework.stereotype.Repository;
@Repository public interface CostCenterRepository extends TenantRepository<CostCenter> {}
