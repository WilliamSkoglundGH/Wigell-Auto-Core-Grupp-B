package com.wac.autocore.service.payment;

import com.wac.autocore.model.Invoice;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice, double amount) {
        // Specifik logg/logik för kortbetalning
        invoice.setPaid(true);
    }

    @Override
    public String getPaymentType() {
        return "CARD";
    }
}