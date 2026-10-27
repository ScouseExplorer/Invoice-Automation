package com.metro.invoice_tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.metro.invoice_tool.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
