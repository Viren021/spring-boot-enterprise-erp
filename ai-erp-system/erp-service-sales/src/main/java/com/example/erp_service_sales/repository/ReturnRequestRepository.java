package com.example.erp_service_sales.repository;
import com.example.erp_service_sales.entity.ReturnRequest; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest,Long> { List<ReturnRequest> findByTenantId(String tenantId); Optional<ReturnRequest> findByIdAndTenantId(Long id,String tenantId);  }
