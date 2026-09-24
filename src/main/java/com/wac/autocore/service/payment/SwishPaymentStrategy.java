package com.wac.autocore.service.payment;

import com.wac.autocore.model.Invoice;
import org.springframework.stereotype.Component;

@Component
public class SwishPaymentStrategy implements PaymentStrategy {
    @Override
    public String processPayment(Invoice invoice, double amount) {
        // Vad ska vara specifikt för denna?
        invoice.setPaid(true);
        return"Invoice id: " + invoice.getId() + " paid by swish.";
    }

    @Override
    public String getPaymentType() {
        return "SWISH";
    }
}