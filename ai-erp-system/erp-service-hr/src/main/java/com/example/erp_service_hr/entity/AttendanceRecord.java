package com.example.erp_service_hr.entity;
import jakarta.persistence.*; import lombok.Data; import java.time.*;
@Entity @Data @Table(name="attendance_records", uniqueConstraints=@UniqueConstraint(columnNames={"employeeId","workDate"}))
public class AttendanceRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long employeeId; private LocalDate workDate; private LocalDateTime checkIn; private LocalDateTime checkOut;
 private String status; private String notes;
}
