package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.SalesOrder; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SalesOrderRepository extends JpaRepository<SalesOrder,Long> { List<SalesOrder> findByTenantId(String tenantId); Optional<SalesOrder> findByIdAndTenantId(Long id,String tenantId); Optional<SalesOrder> findByOrderNumberAndTenantId(String number,String tenantId); }
