package com.example.erp_service_master_data.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter;
@Entity @Table(name="md_currencies",uniqueConstraints=@UniqueConstraint(name="uk_currency_tenant_code",columnNames={"tenant_id","code"}))
@Getter @Setter public class Currency extends TenantEntity {
 @Column(nullable=false,length=3) private String code; @Column(nullable=false,length=100) private String name;
 @Column(length=10) private String symbol; @Column(nullable=false) private int minorUnit=2; @Column(nullable=false) private boolean active=true;
}
