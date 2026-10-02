package com.example.erp_service_finance.repository;
import com.example.erp_service_finance.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    Optional<JournalEntry> findByTenantIdAndEntryNumber(String tenantId, String entryNumber);
    Optional<JournalEntry> findByTenantIdAndSourceEventId(String tenantId, String sourceEventId);
    List<JournalEntry> findByTenantIdOrderByEntryDateDesc(String tenantId);
    Optional<JournalEntry> findByIdAndTenantId(UUID id, String tenantId);
}
