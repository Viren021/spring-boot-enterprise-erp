package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.Customer; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CustomerRepository extends JpaRepository<Customer,Long> { List<Customer> findByTenantId(String tenantId); Optional<Customer> findByIdAndTenantId(Long id,String tenantId); Optional<Customer> findByCodeAndTenantId(String code,String tenantId); }
