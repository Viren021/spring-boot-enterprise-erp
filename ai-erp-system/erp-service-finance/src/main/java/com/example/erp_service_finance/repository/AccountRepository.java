package com.example.erp_service_finance.repository;
import com.example.erp_service_finance.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByTenantIdOrderByCode(String tenantId);
    Optional<Account> findByIdAndTenantId(UUID id, String tenantId);
    Optional<Account> findByTenantIdAndCode(String tenantId, String code);
}
