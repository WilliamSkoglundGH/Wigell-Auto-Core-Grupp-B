package com.wac.autocore.service.payment;

import com.wac.autocore.model.Invoice;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String processPayment(Invoice invoice, BigDecimal amount);
    String getPaymentType();
}