package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.Department; import org.springframework.stereotype.Repository;
@Repository public interface DepartmentRepository extends TenantRepository<Department> {}
