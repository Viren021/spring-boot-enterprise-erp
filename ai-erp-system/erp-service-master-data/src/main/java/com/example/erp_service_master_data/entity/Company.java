package com.example.erp_service_master_data.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter;
@Entity @Table(name="md_companies", uniqueConstraints=@UniqueConstraint(name="uk_company_tenant_code",columnNames={"tenant_id","code"}))
@Getter @Setter public class Company extends TenantEntity {
 @Column(nullable=false,length=30) private String code; @Column(nullable=false,length=200) private String name;
 @Column(length=100) private String registrationNumber; @Column(length=2) private String countryCode;
 @Column(nullable=false,length=3) private String baseCurrencyCode; @Column(nullable=false) private boolean active=true;
}
