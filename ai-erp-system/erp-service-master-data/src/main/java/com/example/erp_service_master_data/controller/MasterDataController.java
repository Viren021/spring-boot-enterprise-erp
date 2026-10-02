package com.example.erp_service_master_data.controller;

import com.example.erp_service_master_data.entity.*;
import com.example.erp_service_master_data.service.MasterDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.example.erp_service_master_data.entity.Currency;

@RestController @RequestMapping("/api/v1/master-data")
public class MasterDataController {
 private final MasterDataService service;
 public MasterDataController(MasterDataService s){service=s;}
 @GetMapping("/companies") public List<Company> companies(){return service.companies();}
 @PostMapping("/companies") @ResponseStatus(HttpStatus.CREATED) public Company company(@Valid @RequestBody Company v){return service.company(v);}
 @GetMapping("/business-units") public List<BusinessUnit> businessUnits(){return service.businessUnits();}
 @PostMapping("/business-units") @ResponseStatus(HttpStatus.CREATED) public BusinessUnit businessUnit(@Valid @RequestBody BusinessUnit v){return service.businessUnit(v);}
 @GetMapping("/departments") public List<Department> departments(){return service.departments();}
 @PostMapping("/departments") @ResponseStatus(HttpStatus.CREATED) public Department department(@Valid @RequestBody Department v){return service.department(v);}
 @GetMapping("/cost-centers") public List<CostCenter> costCenters(){return service.costCenters();}
 @PostMapping("/cost-centers") @ResponseStatus(HttpStatus.CREATED) public CostCenter costCenter(@Valid @RequestBody CostCenter v){return service.costCenter(v);}
 @GetMapping("/tax-codes") public List<TaxCode> taxCodes(){return service.taxCodes();}
 @PostMapping("/tax-codes") @ResponseStatus(HttpStatus.CREATED) public TaxCode taxCode(@Valid @RequestBody TaxCode v){return service.taxCode(v);}
 @GetMapping("/currencies") public List<Currency> currencies(){return service.currencies();}
 @PostMapping("/currencies") @ResponseStatus(HttpStatus.CREATED) public Currency currency(@Valid @RequestBody Currency v){return service.currency(v);}
 @GetMapping("/uoms") public List<Uom> uoms(){return service.uoms();}
 @PostMapping("/uoms") @ResponseStatus(HttpStatus.CREATED) public Uom uom(@Valid @RequestBody Uom v){return service.uom(v);}
 @GetMapping("/document-numbering") public List<DocumentNumbering> numbering(){return service.numbering();}
 @PostMapping("/document-numbering") @ResponseStatus(HttpStatus.CREATED) public DocumentNumbering numbering(@Valid @RequestBody DocumentNumbering v){return service.numbering(v);}
 @PostMapping("/document-numbering/{id}/next") public Map<String,String> next(@PathVariable UUID id){return Map.of("number",service.nextNumber(id));}
}
