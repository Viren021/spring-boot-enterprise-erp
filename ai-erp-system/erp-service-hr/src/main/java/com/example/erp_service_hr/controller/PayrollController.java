package com.example.erp_service_hr.controller;
import com.example.erp_service_hr.entity.*; import com.example.erp_service_hr.service.HrWorkflowService; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/v1/hr/payroll")
public class PayrollController { private final HrWorkflowService service; public PayrollController(HrWorkflowService s){service=s;}
 @GetMapping("/periods") public List<PayrollPeriod> periods(){return service.periods();}
 @PostMapping("/periods") public PayrollPeriod period(@RequestBody PayrollPeriod x){return service.period(x);}
 @GetMapping("/runs") public List<PayrollRun> runs(@RequestParam(required=false) Long periodId){return service.runs(periodId);}
 @PostMapping("/runs") public PayrollRun run(@RequestBody PayrollRun x){return service.run(x);}
 @PostMapping("/runs/{id}/process") public ResponseEntity<PayrollRun> process(@PathVariable Long id){return ResponseEntity.ok(service.processRun(id));}
}
