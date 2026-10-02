package com.example.erp_service_master_data.entity;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter;
@Entity @Table(name="md_document_numbering",uniqueConstraints=@UniqueConstraint(name="uk_numbering_tenant_type",columnNames={"tenant_id","document_type"}))
@Getter @Setter public class DocumentNumbering extends TenantEntity {
 @Column(name="document_type",nullable=false,length=50) private String documentType;
 @Column(nullable=false,length=20) private String prefix; @Column(nullable=false) private long nextNumber=1;
 @Column(nullable=false) private int padding=6; @Column(nullable=false) private boolean active=true;
 public boolean isActive(){return active;} public String getPrefix(){return prefix;}
 public long getNextNumber(){return nextNumber;} public int getPadding(){return padding;}
 public void setNextNumber(long value){nextNumber=value;}
}
