package com.example.erp_service_master_data.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.util.UUID;
@Entity @Table(name="md_cost_centers",uniqueConstraints=@UniqueConstraint(name="uk_cost_center_tenant_code",columnNames={"tenant_id","code"}))
@Getter @Setter public class CostCenter extends TenantEntity {
 @Column(nullable=false,length=30) private String code; @Column(nullable=false,length=200) private String name;
 @Column(nullable=false) private UUID departmentId; @Column(nullable=false) private boolean active=true;
}
