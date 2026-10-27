package com.metro.invoice_tool.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.metro.invoice_tool.dto.CreateInvoiceRequest;
import com.metro.invoice_tool.dto.InvoiceResponse;
import com.metro.invoice_tool.entity.Invoice;
import com.metro.invoice_tool.entity.InvoiceStatus;
import com.metro.invoice_tool.exception.ResourceNotFoundException;
import com.metro.invoice_tool.repository.InvoiceRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setCustomerName(request.getCustomerName());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getIssueDate().plusDays(30));
        invoice.setStatus(InvoiceStatus.DRAFT);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(savedInvoice);
    }

    public List<InvoiceResponse> getInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(InvoiceResponse::fromEntity)
                .toList();
    }

    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return InvoiceResponse.fromEntity(invoice);
    }

    public boolean isPresent(Long id) {
        return invoiceRepository.existsById(id);
    }

    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Invoice", "id", id);
        }
        invoiceRepository.deleteById(id);
    }
}
