package com.example.erp_service_hr.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Data @Table(name="leave_requests")
public class LeaveRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long employeeId; @Enumerated(EnumType.STRING) private LeaveType type;
 private LocalDate startDate; private LocalDate endDate; private String reason;
 @Enumerated(EnumType.STRING) private ApprovalStatus status=ApprovalStatus.PENDING;
 private String approvedBy; private String rejectionReason;
}
