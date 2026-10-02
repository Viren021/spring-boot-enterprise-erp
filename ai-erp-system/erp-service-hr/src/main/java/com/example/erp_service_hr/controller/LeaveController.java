package com.example.erp_service_hr.controller;
import com.example.erp_service_hr.entity.LeaveRequest; import com.example.erp_service_hr.service.HrWorkflowService;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.List;
@RestController @RequestMapping("/api/v1/hr/leaves")
public class LeaveController {
 private final HrWorkflowService service; public LeaveController(HrWorkflowService s){service=s;}
 @GetMapping public List<LeaveRequest> list(@RequestParam(required=false) Long employeeId){return service.leaves(employeeId);}
 @PostMapping public ResponseEntity<LeaveRequest> create(@RequestBody LeaveRequest x){return ResponseEntity.ok(service.leave(x));}
 @PostMapping("/{id}/approve") public ResponseEntity<LeaveRequest> approve(@PathVariable Long id, Principal p){return ResponseEntity.ok(service.decideLeave(id,true,p==null?"system":p.getName(),null));}
 @PostMapping("/{id}/reject") public ResponseEntity<LeaveRequest> reject(@PathVariable Long id,@RequestParam(required=false) String reason,Principal p){return ResponseEntity.ok(service.decideLeave(id,false,p==null?"system":p.getName(),reason));}
}
