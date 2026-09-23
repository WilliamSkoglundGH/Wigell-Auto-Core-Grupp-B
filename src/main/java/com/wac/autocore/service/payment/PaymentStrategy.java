package com.wac.autocore.service.payment;

import com.wac.autocore.model.Invoice;

public interface PaymentStrategy {
    void processPayment(Invoice invoice, double amount);
    String getPaymentType(); // T.ex. "CARD", "SWISH", "CASH"
}