package com.wac.autocore.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", unique = true, nullable = false)
    private WorkOrder workOrder;
    @Column(name = "invoice_date", length = 20, nullable = false)
    private LocalDate invoiceDate;
    @Column(name = "amount", nullable = false)
    private double amount;
    @Column(name = "discount", nullable = false)
    private double discount;
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceLine> invoiceLines = new ArrayList<>();
    @Column(name = "paid", nullable = false)
    private boolean paid;

    protected Invoice() {
    }

    public Invoice(WorkOrder workOrder, LocalDate invoiceDate, double amount, double discount) {
        this.workOrder = workOrder;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.discount = discount;
        this.paid = false;
    }

    public Invoice(WorkOrder workOrder, LocalDate invoiceDate, double amount, double discount, List<InvoiceLine> invoiceLines) {
        this.workOrder = workOrder;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.discount = discount;
        this.invoiceLines = invoiceLines;
        this.paid = false;
    }

    public Long getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {

        return workOrder;
    }

    public void addInvoiceLine(InvoiceLine invoiceLine) {
        invoiceLines.add(invoiceLine);
        invoiceLine.setInvoice(this);
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
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

    public double getTotalAmount() {
        return amount - discount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return id +
                " - Work order ID: " + workOrder.getId() +
                " | Date: " + invoiceDate +
                " | Amount: " + amount + " SEK" +
                " | Discount: " + discount + " SEK" +
                " | Total: " + (amount - discount) + " SEK" +
                " | Paid: " + (paid ? "Yes" : "No");
    }
}