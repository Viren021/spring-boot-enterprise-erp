package com.example.erp_service_master_data.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter;
@Entity @Table(name="md_uoms",uniqueConstraints=@UniqueConstraint(name="uk_uom_tenant_code",columnNames={"tenant_id","code"}))
@Getter @Setter public class Uom extends TenantEntity {
 @Column(nullable=false,length=30) private String code; @Column(nullable=false,length=100) private String name;
 @Column(length=20) private String symbol; @Column(length=50) private String category; @Column(nullable=false) private boolean active=true;
}
