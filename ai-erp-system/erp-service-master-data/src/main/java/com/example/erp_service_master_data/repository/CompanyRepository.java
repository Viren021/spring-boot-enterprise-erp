package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.Company; import org.springframework.stereotype.Repository;
@Repository public interface CompanyRepository extends TenantRepository<Company> {}
