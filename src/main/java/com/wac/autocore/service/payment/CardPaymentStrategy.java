package com.wac.autocore.service.payment;

import com.wac.autocore.model.Invoice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public String processPayment(Invoice invoice, BigDecimal amount) {
        // Vad ska vara specifikt för denna?
        invoice.setPaid(true);
        return "payment.success.CARD";
    }

    @Override
    public String getPaymentType() {
        return "CARD";
    }
}