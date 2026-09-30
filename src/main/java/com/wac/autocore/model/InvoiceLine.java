package com.wac.autocore.model;

import javax.persistence.*;

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
    @Column(name = "amount", nullable = false)
    private double amount;
    @Column(name = "discount", nullable = false)
    private double discount;

    protected InvoiceLine() {
    }

    public InvoiceLine(Invoice invoice, String nameOfService, double amount, double discount) {
        this.invoice = invoice;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}
