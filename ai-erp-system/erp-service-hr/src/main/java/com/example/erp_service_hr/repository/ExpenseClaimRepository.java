package com.example.erp_service_hr.repository;
import com.example.erp_service_hr.entity.ExpenseClaim; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim,Long> { List<ExpenseClaim> findByEmployeeId(Long id); }
