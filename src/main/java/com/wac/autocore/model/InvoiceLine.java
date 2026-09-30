package com.wac.autocore.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_line")
public class InvoiceLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "name_of_service", length = 60, nullable = false)
    private String nameOfService;
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    @Column(name = "discount", precision = 10, scale = 2, nullable = false)

    private BigDecimal discount;

    protected InvoiceLine() {
    }

    public InvoiceLine(String nameOfService,BigDecimal amount, BigDecimal discount) {
        this.nameOfService = nameOfService;
        this.amount = amount;
        this.discount = discount;
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getNameOfService() {
        return nameOfService;
    }

    public void setNameOfService(String nameOfService) {
        this.nameOfService = nameOfService;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}
