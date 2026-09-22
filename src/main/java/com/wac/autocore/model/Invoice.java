package com.wac.autocore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(mappedBy = "work_order")
    private WorkOrder workOrder;
    @Column(name = "invoice_date", length = 20, nullable = false)
    private LocalDate invoiceDate;
    @Column(name = "amount", nullable = false)
    private double amount;
    @Column(name = "discount", nullable = false)
    private double discount;
    /*
    redundant information, remove?

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;
     */
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

    public Long getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {

        return workOrder;
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