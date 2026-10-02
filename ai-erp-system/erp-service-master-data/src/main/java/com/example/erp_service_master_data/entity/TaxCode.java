package com.example.erp_service_master_data.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.math.BigDecimal;
@Entity @Table(name="md_tax_codes",uniqueConstraints=@UniqueConstraint(name="uk_tax_tenant_code",columnNames={"tenant_id","code"}))
@Getter @Setter public class TaxCode extends TenantEntity {
 @Column(nullable=false,length=30) private String code; @Column(nullable=false,length=200) private String name;
 @Column(nullable=false,precision=9,scale=4) private BigDecimal rate; @Column(length=100) private String jurisdiction;
 @Column(nullable=false) private boolean active=true;
}
