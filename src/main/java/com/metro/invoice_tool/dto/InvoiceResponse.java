package com.metro.invoice_tool.dto;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.metro.invoice_tool.entity.Invoice;
import com.metro.invoice_tool.entity.InvoiceStatus;

public class InvoiceResponse {

    private Long id;
    private String customerName;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private boolean overdue;
    private long overdueDays;
    private Instant createdAt;

    public static InvoiceResponse fromEntity(Invoice invoice, Clock clock) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setCustomerName(invoice.getCustomerName());
        response.setAmount(invoice.getAmount());
        response.setIssueDate(invoice.getIssueDate());
        response.setDueDate(invoice.getDueDate());
        response.setStatus(invoice.getStatus());
        response.setOverdue(isOverdue(invoice, clock));
        response.setOverdueDays(calculateOverdueDays(invoice, clock));
        response.setCreatedAt(invoice.getCreatedAt());
        return response;
    }

    private static boolean isOverdue(Invoice invoice, Clock clock) {
        return calculateOverdueDays(invoice, clock) > 0;
    }

    private static long calculateOverdueDays(Invoice invoice, Clock clock) {
        if (invoice.getDueDate() == null || !hasOutstandingBalance(invoice)) {
            return 0;
        }

        LocalDate today = LocalDate.now(clock);
        if (!invoice.getDueDate().isBefore(today)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(invoice.getDueDate(), today);
    }

    private static boolean hasOutstandingBalance(Invoice invoice) {
        return invoice.getStatus() == InvoiceStatus.ISSUED || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public long getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(long overdueDays) {
        this.overdueDays = overdueDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
