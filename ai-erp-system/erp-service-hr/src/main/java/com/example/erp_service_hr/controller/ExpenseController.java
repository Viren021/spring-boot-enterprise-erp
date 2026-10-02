package com.example.erp_service_hr.controller;
import com.example.erp_service_hr.entity.ExpenseClaim; import com.example.erp_service_hr.service.HrWorkflowService; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.security.Principal; import java.util.List;
@RestController @RequestMapping("/api/v1/hr/expenses")
public class ExpenseController { private final HrWorkflowService service; public ExpenseController(HrWorkflowService s){service=s;}
 @GetMapping public List<ExpenseClaim> list(@RequestParam(required=false) Long employeeId){return service.expenses(employeeId);}
 @PostMapping public ExpenseClaim create(@RequestBody ExpenseClaim x){return service.expense(x);}
 @PostMapping("/{id}/approve") public ResponseEntity<ExpenseClaim> approve(@PathVariable Long id,Principal p){return ResponseEntity.ok(service.decideExpense(id,true,p==null?"system":p.getName(),null));}
 @PostMapping("/{id}/reject") public ResponseEntity<ExpenseClaim> reject(@PathVariable Long id,@RequestParam(required=false) String reason,Principal p){return ResponseEntity.ok(service.decideExpense(id,false,p==null?"system":p.getName(),reason));}
}
