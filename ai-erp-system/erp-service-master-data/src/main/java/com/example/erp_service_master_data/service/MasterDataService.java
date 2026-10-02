package com.example.erp_service_master_data.service;

import com.example.erp_service_master_data.config.TenantContext;
import com.example.erp_service_master_data.entity.*;
import com.example.erp_service_master_data.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;
import com.example.erp_service_master_data.entity.Currency;

@Service
public class MasterDataService {
 private final CompanyRepository companies; private final BusinessUnitRepository businessUnits;
 private final DepartmentRepository departments; private final CostCenterRepository costCenters;
 private final TaxCodeRepository taxCodes; private final CurrencyRepository currencies; private final UomRepository uoms;
 private final DocumentNumberingRepository numbering;
 public MasterDataService(CompanyRepository c,BusinessUnitRepository b,DepartmentRepository d,CostCenterRepository cc,
   TaxCodeRepository t,CurrencyRepository cur,UomRepository u,DocumentNumberingRepository n) {
  companies=c; businessUnits=b; departments=d; costCenters=cc; taxCodes=t; currencies=cur; uoms=u; numbering=n;
 }
 private String tenant(){return TenantContext.require();}
 private <T extends TenantEntity> T tenant(T value){value.setId(null); value.setTenantId(tenant()); return value;}
 public List<Company> companies(){return companies.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public Company company(Company v){return companies.save(tenant(v));}
 public List<BusinessUnit> businessUnits(){return businessUnits.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public BusinessUnit businessUnit(BusinessUnit v){return businessUnits.save(tenant(v));}
 public List<Department> departments(){return departments.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public Department department(Department v){return departments.save(tenant(v));}
 public List<CostCenter> costCenters(){return costCenters.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public CostCenter costCenter(CostCenter v){return costCenters.save(tenant(v));}
 public List<TaxCode> taxCodes(){return taxCodes.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public TaxCode taxCode(TaxCode v){return taxCodes.save(tenant(v));}
 public List<Currency> currencies(){return currencies.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public Currency currency(Currency v){return currencies.save(tenant(v));}
 public List<Uom> uoms(){return uoms.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public Uom uom(Uom v){return uoms.save(tenant(v));}
 public List<DocumentNumbering> numbering(){return numbering.findAllByTenantIdOrderByCreatedAtDesc(tenant());}
 public DocumentNumbering numbering(DocumentNumbering v){return numbering.save(tenant(v));}
 @Transactional public String nextNumber(UUID id) {
  DocumentNumbering n=numbering.lockByIdAndTenant(id,tenant()).orElseThrow(NoSuchElementException::new);
  if(!n.isActive()) throw new IllegalStateException("Document numbering is inactive");
  String result=n.getPrefix()+String.format("%0"+n.getPadding()+"d",n.getNextNumber());
  n.setNextNumber(n.getNextNumber()+1); return result;
 }
}
