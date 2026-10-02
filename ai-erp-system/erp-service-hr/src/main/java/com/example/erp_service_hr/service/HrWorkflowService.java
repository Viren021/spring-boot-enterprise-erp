package com.example.erp_service_hr.service;

import com.example.erp_service_hr.entity.*;
import com.example.erp_service_hr.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HrWorkflowService {
 private final LeaveRequestRepository leaves; private final AttendanceRecordRepository attendance;
 private final PayrollPeriodRepository periods; private final PayrollRunRepository runs; private final ExpenseClaimRepository expenses;
 public HrWorkflowService(LeaveRequestRepository l, AttendanceRecordRepository a, PayrollPeriodRepository p, PayrollRunRepository r, ExpenseClaimRepository e) { leaves=l; attendance=a; periods=p; runs=r; expenses=e; }
 public List<LeaveRequest> leaves(Long employeeId) { return employeeId == null ? leaves.findAll() : leaves.findByEmployeeId(employeeId); }
 @Transactional public LeaveRequest leave(LeaveRequest value) {
  if(value.getEmployeeId()==null || value.getStartDate()==null || value.getEndDate()==null || value.getEndDate().isBefore(value.getStartDate())) throw new IllegalArgumentException("Invalid leave request");
  return leaves.save(value);
 }
 @Transactional public LeaveRequest decideLeave(Long id, boolean approve, String actor, String reason) { LeaveRequest x=leaves.findById(id).orElseThrow(); ensurePending(x.getStatus()); x.setStatus(approve?ApprovalStatus.APPROVED:ApprovalStatus.REJECTED); x.setApprovedBy(actor); x.setRejectionReason(approve?null:reason); return leaves.save(x); }
 public List<AttendanceRecord> attendance(Long employeeId) { return employeeId==null?attendance.findAll():attendance.findByEmployeeId(employeeId); }
 @Transactional public AttendanceRecord recordAttendance(AttendanceRecord x) { if(x.getEmployeeId()==null||x.getWorkDate()==null) throw new IllegalArgumentException("Employee and work date are required"); if(x.getCheckIn()!=null&&x.getCheckOut()!=null&&x.getCheckOut().isBefore(x.getCheckIn())) throw new IllegalArgumentException("Check-out precedes check-in"); return attendance.save(x); }
 @Transactional public PayrollPeriod period(PayrollPeriod x) { if(x.getStartDate()==null||x.getEndDate()==null||x.getEndDate().isBefore(x.getStartDate())) throw new IllegalArgumentException("Invalid payroll period"); return periods.save(x); }
 public List<PayrollPeriod> periods(){return periods.findAll();}
 @Transactional public PayrollRun run(PayrollRun x) { if(x.getPeriodId()==null||x.getEmployeeId()==null) throw new IllegalArgumentException("Period and employee are required"); if(x.getGrossAmount()==null) throw new IllegalArgumentException("Gross amount is required"); if(x.getDeductions()==null) x.setDeductions(java.math.BigDecimal.ZERO); x.setNetAmount(x.getGrossAmount().subtract(x.getDeductions())); return runs.save(x); }
 public List<PayrollRun> runs(Long periodId){return periodId==null?runs.findAll():runs.findByPeriodId(periodId);}
 @Transactional public PayrollRun processRun(Long id){PayrollRun x=runs.findById(id).orElseThrow(); ensurePending(x.getStatus()); x.setStatus(ApprovalStatus.APPROVED); x.setProcessedAt(LocalDateTime.now()); return runs.save(x);}
 @Transactional public ExpenseClaim expense(ExpenseClaim x){if(x.getEmployeeId()==null||x.getAmount()==null||x.getAmount().signum()<0)throw new IllegalArgumentException("Invalid expense claim");return expenses.save(x);}
 public List<ExpenseClaim> expenses(Long employeeId){return employeeId==null?expenses.findAll():expenses.findByEmployeeId(employeeId);}
 @Transactional public ExpenseClaim decideExpense(Long id,boolean approve,String actor,String reason){ExpenseClaim x=expenses.findById(id).orElseThrow();ensurePending(x.getStatus());x.setStatus(approve?ApprovalStatus.APPROVED:ApprovalStatus.REJECTED);x.setApprovedBy(actor);x.setRejectionReason(approve?null:reason);return expenses.save(x);}
 private void ensurePending(ApprovalStatus s){if(s!=ApprovalStatus.PENDING)throw new IllegalStateException("Record has already been decided");}
}
