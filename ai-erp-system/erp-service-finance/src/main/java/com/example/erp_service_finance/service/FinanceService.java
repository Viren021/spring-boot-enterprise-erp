package com.example.erp_service_finance.service;

import com.example.erp_service_finance.config.TenantContext;
import com.example.erp_service_finance.dto.*;
import com.example.erp_service_finance.entity.*;
import com.example.erp_service_finance.model.PostingStatus;
import com.example.erp_service_finance.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FinanceService {
    private final AccountRepository accounts;
    private final FiscalPeriodRepository periods;
    private final JournalEntryRepository journals;

    public FinanceService(AccountRepository accounts, FiscalPeriodRepository periods, JournalEntryRepository journals) {
        this.accounts = accounts; this.periods = periods; this.journals = journals;
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        requireText(request.code(), "Account code");
        requireText(request.name(), "Account name");
        if (request.type() == null) throw new IllegalArgumentException("Account type is required");
        Account account = new Account();
        account.setTenantId(TenantContext.requireTenantId());
        account.setCode(request.code().trim()); account.setName(request.name().trim()); account.setType(request.type());
        return accounts.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> accounts() { return accounts.findByTenantIdOrderByCode(TenantContext.requireTenantId()); }

    @Transactional
    public FiscalPeriod createPeriod(FiscalPeriodRequest request) {
        requireText(request.name(), "Period name");
        if (request.startDate() == null || request.endDate() == null || request.startDate().isAfter(request.endDate()))
            throw new IllegalArgumentException("Fiscal period dates are invalid");
        FiscalPeriod period = new FiscalPeriod();
        period.setTenantId(TenantContext.requireTenantId()); period.setName(request.name().trim());
        period.setStartDate(request.startDate()); period.setEndDate(request.endDate());
        return periods.save(period);
    }

    @Transactional(readOnly = true)
    public List<FiscalPeriod> periods() { return periods.findByTenantIdOrderByStartDate(TenantContext.requireTenantId()); }

    @Transactional
    public JournalEntry createJournal(JournalEntryRequest request) {
        return createJournal(request, "system");
    }

    @Transactional
    public JournalEntry createJournal(JournalEntryRequest request, String actor) {
        String tenant = TenantContext.requireTenantId();
        requireText(request.entryNumber(), "Entry number");
        if (request.entryDate() == null || request.lines() == null || request.lines().size() < 2)
            throw new IllegalArgumentException("A journal requires a date and at least two lines");
        validateBalanced(request.lines());
        if (periods.findByTenantIdOrderByStartDate(tenant).stream().noneMatch(p ->
                p.getStatus() != com.example.erp_service_finance.model.PeriodStatus.CLOSED &&
                !request.entryDate().isBefore(p.getStartDate()) && !request.entryDate().isAfter(p.getEndDate())))
            throw new IllegalArgumentException("Entry date is not in an open fiscal period");
        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenant); entry.setEntryNumber(request.entryNumber().trim());
        entry.setEntryDate(request.entryDate()); entry.setDescription(request.description());
        entry.setCreatedBy(actor == null || actor.isBlank() ? "system" : actor);
        for (JournalLineRequest lineRequest : request.lines()) {
            if (lineRequest == null || lineRequest.accountId() == null)
                throw new IllegalArgumentException("Every journal line requires an account");
            Account account = accounts.findByIdAndTenantId(lineRequest.accountId(), tenant)
                    .orElseThrow(() -> new IllegalArgumentException("Account does not belong to this tenant"));
            JournalLine line = new JournalLine();
            line.setAccount(account); line.setDebit(value(lineRequest.debit())); line.setCredit(value(lineRequest.credit()));
            line.setDescription(lineRequest.description()); validateLine(line);
            entry.addLine(line);
        }
        return journals.save(entry);
    }

    /**
     * Records an order as a posted double-entry sale.  Account setup is
     * intentional: finance administrators must create AR and SALES accounts
     * rather than this integration silently inventing accounts or prices.
     */
    @Transactional
    public JournalEntry recordOrder(OrderEvent event) {
        String tenant = TenantContext.requireTenantId();
        if (event == null || event.getOrderId() == null) {
            throw new IllegalArgumentException("Order event and orderId are required");
        }

        String entryNumber = "ORDER-" + event.getOrderId();
        String suppliedEventId = event.getEventId();
        if (suppliedEventId == null || suppliedEventId.isBlank()) suppliedEventId = event.getSourceEventId();
        String sourceEventId = suppliedEventId == null || suppliedEventId.isBlank()
                ? entryNumber : suppliedEventId.trim();

        Optional<JournalEntry> existing = journals.findByTenantIdAndSourceEventId(tenant, sourceEventId);
        if (existing.isEmpty()) existing = journals.findByTenantIdAndEntryNumber(tenant, entryNumber);
        if (existing.isPresent()) return existing.get();

        BigDecimal amount = event.getAmount();
        if (amount == null && event.getUnitPrice() != null) {
            if (event.getQuantity() == null || event.getQuantity() <= 0)
                throw new IllegalArgumentException("Positive quantity is required with unitPrice");
            amount = event.getUnitPrice().multiply(BigDecimal.valueOf(event.getQuantity()));
        } else if (amount != null && event.getUnitPrice() != null && event.getQuantity() != null
                && event.getQuantity() > 0
                && amount.compareTo(event.getUnitPrice().multiply(BigDecimal.valueOf(event.getQuantity()))) != 0) {
            throw new IllegalArgumentException("amount and unitPrice * quantity do not match");
        }
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("A positive amount or unitPrice is required");

        Account receivable = accounts.findByTenantIdAndCode(tenant, "AR")
                .orElseThrow(() -> new IllegalArgumentException("Finance account AR is not configured"));
        Account sales = accounts.findByTenantIdAndCode(tenant, "SALES")
                .orElseThrow(() -> new IllegalArgumentException("Finance account SALES is not configured"));
        JournalEntry entry = createJournal(new JournalEntryRequest(entryNumber, LocalDate.now(),
                "Order " + event.getOrderId(), List.of(
                new JournalLineRequest(receivable.getId(), amount, BigDecimal.ZERO, "Order receivable"),
                new JournalLineRequest(sales.getId(), BigDecimal.ZERO, amount, "Order revenue"))));
        entry.setSourceEventId(sourceEventId);
        journals.save(entry);
        return postJournal(entry.getId());
    }

    /**
     * Converts approved cross-service business events into posted journals.
     * Account codes are configuration, not silently-created integration data.
     */
    @Transactional
    public JournalEntry recordIntegrationEvent(FinanceIntegrationEvent event) {
        String tenant = TenantContext.requireTenantId();
        if (event == null || event.eventType() == null || event.amount() == null
                || event.amount().signum() <= 0 || event.eventId() == null || event.eventId().isBlank()) {
            throw new IllegalArgumentException("eventType, eventId and positive amount are required");
        }
        Optional<JournalEntry> existing = journals.findByTenantIdAndSourceEventId(tenant, event.eventId());
        if (existing.isPresent()) return existing.get();

        String type = event.eventType().trim().toUpperCase();
        String debitCode;
        String creditCode;
        if ("PROCUREMENT_INVOICE".equals(type)) {
            debitCode = "INVENTORY"; creditCode = "AP";
        } else if ("GOODS_RECEIPT".equals(type)) {
            debitCode = "INVENTORY"; creditCode = "GRNI";
        } else if ("SALES_INVOICE".equals(type)) {
            debitCode = "AR"; creditCode = "SALES";
        } else if ("SALES_PAYMENT".equals(type)) {
            debitCode = "CASH"; creditCode = "AR";
        } else if ("PAYROLL_APPROVED".equals(type)) {
            debitCode = "PAYROLL_EXPENSE"; creditCode = "PAYROLL_PAYABLE";
        } else if ("INVENTORY_VALUATION".equals(type) || "INVENTORY_STOCK_MOVEMENT".equals(type)
                || "INVENTORY_RESERVATION".equals(type)) {
            boolean decrease = "DECREASE".equalsIgnoreCase(event.direction());
            debitCode = decrease ? "COGS" : "INVENTORY";
            creditCode = decrease ? "INVENTORY" : "COGS";
        } else {
            throw new IllegalArgumentException("Unsupported finance event type: " + event.eventType());
        }
        Account debit = accounts.findByTenantIdAndCode(tenant, debitCode)
                .orElseThrow(() -> new IllegalArgumentException("Finance account " + debitCode + " is not configured"));
        Account credit = accounts.findByTenantIdAndCode(tenant, creditCode)
                .orElseThrow(() -> new IllegalArgumentException("Finance account " + creditCode + " is not configured"));
        String source = event.sourceDocumentId() == null ? event.eventId() : event.sourceDocumentId();
        JournalEntry entry = createJournal(new JournalEntryRequest(
                "INTEGRATION-" + source, event.eventDate() == null ? LocalDate.now() : event.eventDate(),
                event.description() == null ? type : event.description(),
                List.of(new JournalLineRequest(debit.getId(), event.amount(), BigDecimal.ZERO, type + " debit"),
                        new JournalLineRequest(credit.getId(), BigDecimal.ZERO, event.amount(), type + " credit"))));
        entry.setSourceEventId(event.eventId());
        journals.save(entry);
        return postJournal(entry.getId());
    }

    @Transactional
    public JournalEntry postJournal(UUID id) {
        return postJournal(id, "system");
    }

    @Transactional
    public JournalEntry postJournal(UUID id, String actor) {
        String tenant = TenantContext.requireTenantId();
        JournalEntry entry = journals.findByIdAndTenantId(id, tenant)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));
        if (entry.getStatus() == PostingStatus.POSTED) throw new IllegalStateException("Journal entry is already posted");
        String approver = actor == null || actor.isBlank() ? "system" : actor;
        if (!"system".equalsIgnoreCase(approver) && approver.equalsIgnoreCase(entry.getCreatedBy())) {
            throw new IllegalStateException("Maker-checker policy prevents the journal creator from posting it");
        }
        validateBalanced(entry.getLines().stream().map(l -> new JournalLineRequest(
                l.getAccount().getId(), l.getDebit(), l.getCredit(), l.getDescription())).toList());
        entry.setStatus(PostingStatus.POSTED);
        entry.setApprovedBy(approver);
        return entry;
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> journals() { return journals.findByTenantIdOrderByEntryDateDesc(TenantContext.requireTenantId()); }

    private static void validateBalanced(List<JournalLineRequest> lines) {
        BigDecimal debit = BigDecimal.ZERO, credit = BigDecimal.ZERO;
        for (JournalLineRequest request : lines) {
            JournalLine line = new JournalLine();
            line.setDebit(value(request.debit())); line.setCredit(value(request.credit())); validateLine(line);
            debit = debit.add(line.getDebit()); credit = credit.add(line.getCredit());
        }
        if (debit.signum() == 0 || debit.compareTo(credit) != 0)
            throw new IllegalArgumentException("Journal debits and credits must be equal and greater than zero");
    }
    private static void validateLine(JournalLine line) {
        if (line.getDebit().signum() < 0 || line.getCredit().signum() < 0 ||
                (line.getDebit().signum() > 0 && line.getCredit().signum() > 0) ||
                line.getDebit().signum() == 0 && line.getCredit().signum() == 0)
            throw new IllegalArgumentException("A line must contain either a positive debit or credit");
    }
    private static BigDecimal value(BigDecimal amount) { return amount == null ? BigDecimal.ZERO : amount; }
    private static void requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required");
    }
}
