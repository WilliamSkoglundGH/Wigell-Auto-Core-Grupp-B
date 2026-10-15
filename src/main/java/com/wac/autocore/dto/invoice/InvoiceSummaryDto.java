package com.wac.autocore.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceSummaryDto {

    private Long id;
    private Long workOrderId;
    private LocalDate invoiceDate;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private boolean paid;

    public InvoiceSummaryDto() {
    }

    public InvoiceSummaryDto(Long id, Long workOrderId, LocalDate invoiceDate,
                             BigDecimal amount, BigDecimal discount,
                             BigDecimal totalAmount, boolean paid) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.discount = discount;
        this.totalAmount = totalAmount;
        this.paid = paid;
    }

    public Long getId() { return id; }
    public Long getWorkOrderId() { return workOrderId; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public boolean isPaid() { return paid; }

    public void setId(Long id) { this.id = id; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setPaid(boolean paid) { this.paid = paid; }

}


