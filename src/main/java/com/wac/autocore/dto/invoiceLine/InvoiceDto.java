package com.wac.autocore.dto.invoiceLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class InvoiceDto {
    //Dto för invoice. Så man kommer förbi LazyLoading när man plocker från databas
    private final Long id;
    private final Long workOrderId;
    private final LocalDate invoiceDate;
    private final BigDecimal amount;
    private final BigDecimal discount;
    private final BigDecimal totalAmount;
    private final boolean paid;
    private final List<InvoiceLineDto> lines;

    public InvoiceDto(Long id, Long workOrderId, LocalDate invoiceDate,
                      BigDecimal amount, BigDecimal discount,
                      BigDecimal totalAmount, boolean paid,
                      List<InvoiceLineDto> lines) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.discount = discount;
        this.totalAmount = totalAmount;
        this.paid = paid;
        this.lines = Collections.unmodifiableList(lines);
    }

    public Long getId() { return id; }
    public Long getWorkOrderId() { return workOrderId; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public BigDecimal getAmount() { return amount; }

    public BigDecimal getDiscount() { return discount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public boolean isPaid() { return paid; }
    public List<InvoiceLineDto> getLines() { return lines; }
}
