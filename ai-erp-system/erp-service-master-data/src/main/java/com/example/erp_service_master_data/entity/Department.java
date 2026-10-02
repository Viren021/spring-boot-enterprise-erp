package com.example.erp_service_master_data.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.util.UUID;
@Entity @Table(name="md_departments",uniqueConstraints=@UniqueConstraint(name="uk_department_tenant_code",columnNames={"tenant_id","code"}))
@Getter @Setter public class Department extends TenantEntity {
 @Column(nullable=false,length=30) private String code; @Column(nullable=false,length=200) private String name;
 @Column(nullable=false) private UUID businessUnitId; @Column(nullable=false) private boolean active=true;
}
