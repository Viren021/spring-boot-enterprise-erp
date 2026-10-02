package com.example.erp_service_finance.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "finance_journal_lines")
@Getter @Setter @NoArgsConstructor
public class JournalLine {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "journal_entry_id", nullable = false)
    @JsonIgnore
    private JournalEntry journalEntry;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal debit = BigDecimal.ZERO;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal credit = BigDecimal.ZERO;
    @Column(length = 500) private String description;
}
