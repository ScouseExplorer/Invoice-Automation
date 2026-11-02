package com.metro.invoice_tool.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.metro.invoice_tool.dto.CreateInvoiceRequest;
import com.metro.invoice_tool.dto.DeleteInvoiceRequest;
import com.metro.invoice_tool.dto.InvoiceResponse;
import com.metro.invoice_tool.dto.UpdateInvoiceRequest;
import com.metro.invoice_tool.entity.Customer;
import com.metro.invoice_tool.entity.Invoice;
import com.metro.invoice_tool.entity.InvoiceStatus;
import com.metro.invoice_tool.exception.ResourceNotFoundException;
import com.metro.invoice_tool.repository.CustomerRepository;
import com.metro.invoice_tool.repository.InvoiceRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final Clock clock = Clock.systemDefaultZone();

    public InvoiceService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setCustomerName(request.getCustomerName());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getIssueDate().plusDays(30));
        invoice.setStatus(InvoiceStatus.DRAFT);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        int year = LocalDate.now(clock).getYear();
        savedInvoice.setInvoiceNumber(String.format("INV-%d-%04d", year, savedInvoice.getId()));
        savedInvoice = invoiceRepository.save(savedInvoice);
        return InvoiceResponse.fromEntity(savedInvoice, clock);
    }

    public List<InvoiceResponse> getInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(invoice -> InvoiceResponse.fromEntity(invoice, clock))
                .toList();
    }

    public List<InvoiceResponse> getOverdueInvoices() {
        LocalDate today = LocalDate.now(clock);
        List<InvoiceStatus> unpaidStatuses = Arrays.asList(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID);

        return invoiceRepository.findByDueDateBeforeAndStatusIn(today, unpaidStatuses)
                .stream()
                .map(invoice -> InvoiceResponse.fromEntity(invoice, clock))
                .toList();
    }

    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return InvoiceResponse.fromEntity(invoice, clock);
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

    public InvoiceResponse deleteInvoice(Long id, DeleteInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        invoiceRepository.delete(invoice);
        return InvoiceResponse.fromEntity(invoice, clock);
    }

    public InvoiceResponse updateInvoice(Long id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        invoice.setCustomerName(request.getCustomerName());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getIssueDate().plusDays(30));
        invoice.setStatus(request.getStatus());

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(updatedInvoice, clock);
    }

    public List<InvoiceResponse> getInvoicesByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        return invoiceRepository.findByCustomerName(customer.getName())
                .stream()
                .map(invoice -> InvoiceResponse.fromEntity(invoice, clock))
                .toList();
    }

    public InvoiceResponse updateInvoiceCustomer(Long customerId, UpdateInvoiceRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Invoice invoice = invoiceRepository.findByCustomerName(customer.getName())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "customerId", customerId));

        invoice.setCustomerName(request.getCustomerName());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getIssueDate().plusDays(30));
        invoice.setStatus(request.getStatus());

        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice), clock);
    }

    public InvoiceResponse generateInvoiceNumber(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            int year = LocalDate.now(clock).getYear();
            invoice.setInvoiceNumber(String.format("INV-%d-%04d", year, invoice.getId()));
        }
        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice), clock);
    }
}
