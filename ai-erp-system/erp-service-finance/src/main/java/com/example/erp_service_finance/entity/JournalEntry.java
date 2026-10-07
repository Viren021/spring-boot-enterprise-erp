package com.example.erp_service_finance.entity;

import com.example.erp_service_finance.model.PostingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "finance_journal_entries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_finance_journal_tenant_number", columnNames = {"tenant_id", "entry_number"}),
                @UniqueConstraint(name = "uk_finance_journal_tenant_source_event", columnNames = {"tenant_id", "source_event_id"})
        })
@Getter @Setter @NoArgsConstructor
public class JournalEntry {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", nullable = false, updatable = false, length = 100) private String tenantId;
    @Column(name = "entry_number", nullable = false, length = 50) private String entryNumber;
    @Column(nullable = false) private LocalDate entryDate;
    @Column(length = 500) private String description;
    @Column(name = "source_event_id", length = 200) private String sourceEventId;
    @Column(name = "created_by", length = 200) private String createdBy;
    @Column(name = "approved_by", length = 200) private String approvedBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PostingStatus status = PostingStatus.DRAFT;
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalLine> lines = new ArrayList<>();

    public void addLine(JournalLine line) {
        lines.add(line);
        line.setJournalEntry(this);
    }
}
