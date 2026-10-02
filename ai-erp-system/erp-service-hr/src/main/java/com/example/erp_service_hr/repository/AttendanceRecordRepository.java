package com.example.erp_service_hr.repository;
import com.example.erp_service_hr.entity.AttendanceRecord; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord,Long> { List<AttendanceRecord> findByEmployeeId(Long id); Optional<AttendanceRecord> findByEmployeeIdAndWorkDate(Long id, java.time.LocalDate date); }
