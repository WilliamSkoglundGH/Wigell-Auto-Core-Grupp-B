package com.wac.autocore.service.payment;

import com.wac.autocore.model.Invoice;
import org.springframework.stereotype.Component;

@Component
public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(Invoice invoice, double amount) {
        invoice.setPaid(true);
    }

    @Override
    public String getPaymentType() {
        return "CASH";
    }
}