package com.example.erp_service_sales.controller;
import com.example.erp_service_sales.entity.*; import com.example.erp_service_sales.service.SalesService; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/sales") public class SalesController { private final SalesService s; public SalesController(SalesService s){this.s=s;} public record Lines<T>(T document,List<SalesOrderLine> lines){} public record QuoteRequest(Quotation quotation,List<QuotationLine> lines){} public record OrderRequest(SalesOrder order,List<SalesOrderLine> lines){}
 @GetMapping("/quotations") public List<Quotation> quotes(){return s.quotations();} @PostMapping("/quotations") public Quotation quote(@RequestBody QuoteRequest r){return s.quotation(r.quotation(),r.lines());}
 @GetMapping("/orders") public List<SalesOrder> orders(){return s.orders();} @GetMapping("/orders/{id}") public SalesOrder order(@PathVariable Long id){return s.order(id);} @PostMapping("/orders") public SalesOrder order(@RequestBody OrderRequest r){return s.order(r.order(),r.lines());}
 @GetMapping("/orders/{id}/credit-check") public Map<String,Boolean> creditCheck(@PathVariable Long id){return Map.of("approved",s.creditCheck(id));}
 @PatchMapping("/orders/{id}/fulfillment-status") public SalesOrder fulfillment(@PathVariable Long id,@RequestBody Map<String,String> body){return s.fulfillmentStatus(id,body.get("status"));}
}
