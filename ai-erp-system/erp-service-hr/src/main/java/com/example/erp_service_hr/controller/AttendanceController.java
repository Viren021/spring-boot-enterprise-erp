package com.example.erp_service_hr.controller;
import com.example.erp_service_hr.entity.AttendanceRecord; import com.example.erp_service_hr.service.HrWorkflowService; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/v1/hr/attendance")
public class AttendanceController { private final HrWorkflowService service; public AttendanceController(HrWorkflowService s){service=s;}
 @GetMapping public List<AttendanceRecord> list(@RequestParam(required=false) Long employeeId){return service.attendance(employeeId);}
 @PostMapping public AttendanceRecord record(@RequestBody AttendanceRecord x){return service.recordAttendance(x);}
}
