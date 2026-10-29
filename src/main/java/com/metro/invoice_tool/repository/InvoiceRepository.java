package com.metro.invoice_tool.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.metro.invoice_tool.entity.Invoice;
import com.metro.invoice_tool.entity.InvoiceStatus;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	List<Invoice> findByDueDateBeforeAndStatusIn(LocalDate dueDate, Collection<InvoiceStatus> statuses);
}
