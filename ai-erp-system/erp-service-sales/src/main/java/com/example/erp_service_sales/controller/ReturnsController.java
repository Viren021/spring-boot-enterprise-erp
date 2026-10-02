package com.example.erp_service_sales.controller;
import com.example.erp_service_sales.entity.*; import com.example.erp_service_sales.service.SalesService; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/sales/returns") public class ReturnsController { private final SalesService s; public ReturnsController(SalesService s){this.s=s;} @PostMapping public CreditNote request(@RequestBody ReturnRequest r){return s.returnAndCredit(r);} }
