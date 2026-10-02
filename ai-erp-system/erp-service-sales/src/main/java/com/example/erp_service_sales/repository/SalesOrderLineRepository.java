package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.SalesOrderLine; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine,Long> { List<SalesOrderLine> findByTenantId(String tenantId); Optional<SalesOrderLine> findByIdAndTenantId(Long id,String tenantId); List<SalesOrderLine> findByOrderIdAndTenantId(Long orderId,String tenantId); }
