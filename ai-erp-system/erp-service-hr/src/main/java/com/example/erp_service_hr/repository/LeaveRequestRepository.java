package com.example.erp_service_hr.repository;
import com.example.erp_service_hr.entity.LeaveRequest; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long> { List<LeaveRequest> findByEmployeeId(Long employeeId); }
