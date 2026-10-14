package com.wac.autocore.dto.invoice;

import com.wac.autocore.dto.invoice.InvoiceLineDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceDetailsDto {

    private Long id;
    private Long workOrderId;
    private LocalDate invoiceDate;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private boolean paid;
    private List<InvoiceLineDto> lines;

    public InvoiceDetailsDto() {
    }

    public InvoiceDetailsDto(Long id, Long workOrderId, LocalDate invoiceDate,
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
        this.lines = lines;
    }

    public Long getId() { return id; }
    public Long getWorkOrderId() { return workOrderId; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public boolean isPaid() { return paid; }
    public List<InvoiceLineDto> getLines() { return lines; }

    public void setId(Long id) { this.id = id; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public void setLines(List<InvoiceLineDto> lines) { this.lines = lines; }
}


