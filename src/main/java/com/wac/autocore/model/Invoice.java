package com.wac.autocore.model;

import java.math.BigDecimal;
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
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    @Column(name = "discount", precision = 10, scale = 2, nullable = false)

    private BigDecimal discount;
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InvoiceLine> invoiceLines = new ArrayList<>();
    @Column(name = "paid", nullable = false)
    private boolean paid;

    protected Invoice() {
    }

    public Invoice(WorkOrder workOrder, LocalDate invoiceDate, BigDecimal amount, BigDecimal discount) {
        this.workOrder = workOrder;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.discount = discount;
        this.paid = false;
    }

    public Invoice(WorkOrder workOrder, LocalDate invoiceDate, BigDecimal amount, BigDecimal discount, List<InvoiceLine> invoiceLines) {
        this.workOrder = workOrder;
        this.invoiceDate = invoiceDate;
        this.amount = amount;
        this.discount = discount;
        addInvoiceLine(invoiceLines);
        this.paid = false;
    }

    public Long getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {

        return workOrder;
    }

    public void addInvoiceLine(List<InvoiceLine> invoiceLine) {
        for (InvoiceLine i : invoiceLine) {
        invoiceLines.add(i);
        i.setInvoice(this);
        }
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

    public BigDecimal getTotalAmount() {

        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (discount == null) {
            return amount;
        }


        return amount.subtract(discount);
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
                " | Total: " + (amount.subtract(discount)) + " SEK" +
                " | Paid: " + (paid ? "Yes" : "No");
    }
}