package com.wac.autocore.dto.invoiceLine;

import java.math.BigDecimal;

public class InvoiceLineDto {
    //Dto för  servicitems-raderna på fakturaspec
    private final Long id;
    private final String nameOfService;
    private final BigDecimal amount;
    private final BigDecimal discount;
    private final BigDecimal total;

    public InvoiceLineDto(Long id, String nameOfService, BigDecimal amount,
                          BigDecimal discount, BigDecimal total) {
        this.id = id;
        this.nameOfService = nameOfService;
        this.amount = amount;
        this.discount = discount;
        this.total = total;
    }

    public Long getId() { return id; }
    public String getNameOfService() { return nameOfService; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getTotalAmount() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (discount == null) {
            return amount;
        }
        return amount.subtract(discount);
    }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotal() { return total; }

}
