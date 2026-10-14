package com.wac.autocore.dto.invoice;

import java.math.BigDecimal;

public class InvoiceLineDto {

    private Long id;
    private String nameOfService;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal totalAmount;

    public InvoiceLineDto() {
    }

    public InvoiceLineDto(Long id, String nameOfService, BigDecimal amount,
                          BigDecimal discount, BigDecimal totalAmount) {
        this.id = id;
        this.nameOfService = nameOfService;
        this.amount = amount;
        this.discount = discount;
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public String getNameOfService() {
        return nameOfService;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNameOfService(String nameOfService) {
        this.nameOfService = nameOfService;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

