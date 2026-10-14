package com.wac.autocore.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentSummaryDto {

    private Long id;
    private Long invoiceId;
    private BigDecimal amount;
    private String paymentType;
    private LocalDateTime paymentDate;
    private boolean successful;

    // Tom konstruktor för ramverk
    public PaymentSummaryDto() {
    }

    // Fullständig konstruktor
    public PaymentSummaryDto(Long id, Long invoiceId, BigDecimal amount, String paymentType, LocalDateTime paymentDate, boolean successful) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentDate = paymentDate;
        this.successful = successful;
    }

    // Getters och Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}