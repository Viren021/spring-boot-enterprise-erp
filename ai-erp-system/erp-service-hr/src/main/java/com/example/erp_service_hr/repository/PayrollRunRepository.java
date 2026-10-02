package com.example.erp_service_hr.repository;
import com.example.erp_service_hr.entity.PayrollRun; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface PayrollRunRepository extends JpaRepository<PayrollRun,Long> { List<PayrollRun> findByPeriodId(Long id); }
