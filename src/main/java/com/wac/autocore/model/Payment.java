package com.wac.autocore.model;

import javax.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    @Column(name = "amount", nullable = false)
    private double amount;
    @Column(name = "payment_type", length = 20, nullable = false)
    private String paymentType;
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    private boolean successful;

    protected Payment() {
    }

    public Payment(Invoice invoice, double amount, String paymentType) {
        this.invoice = invoice;
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentDate = LocalDateTime.now(); // Sätter nuvarande tid automatiskt
        this.successful = true;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
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

    @Override
    public String toString() {
        return id +
                " - Invoice ID: " + invoice.getId() +
                " | Amount: " + amount + " SEK" +
                " | Payment type: " + paymentType +
                " | Date: " + paymentDate +
                " | Successful: " + (successful ? "Yes" : "No");
    }
}