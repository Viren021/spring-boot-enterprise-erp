package com.example.erp_service_master_data.repository;
import com.example.erp_service_master_data.entity.DocumentNumbering;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import org.springframework.stereotype.Repository; import jakarta.persistence.LockModeType;
import java.util.*;
@Repository public interface DocumentNumberingRepository extends TenantRepository<DocumentNumbering> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select n from DocumentNumbering n where n.id=:id and n.tenantId=:tenant")
 Optional<DocumentNumbering> lockByIdAndTenant(@Param("id") UUID id,@Param("tenant") String tenant);
}
