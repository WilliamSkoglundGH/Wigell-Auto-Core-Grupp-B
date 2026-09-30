package com.wac.autocore.service.payment;

import com.wac.autocore.model.Invoice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SwishPaymentStrategy implements PaymentStrategy {
    @Override
    public String processPayment(Invoice invoice, BigDecimal amount) {
        // Vad ska vara specifikt för denna?
        invoice.setPaid(true);
        return "payment.success.SWISH";
    }

    @Override
    public String getPaymentType() {
        return "SWISH";
    }
}