package com.wac.autocore.service;

import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.Payment;
import com.wac.autocore.repository.InvoiceRepository;
import com.wac.autocore.repository.PaymentRepository;
import com.wac.autocore.service.payment.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private final Map<String, PaymentStrategy> strategies;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(Map<String, PaymentStrategy> strategies, PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.strategies = strategies;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    @Transactional
    public String processPayment(Long invoiceId, String paymentType) {
        // 1. Hämta fakturan från databasen
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice with ID " + invoiceId + " was not found."));


        PaymentStrategy strategy = strategies.values().stream()
                .filter(s -> s.getPaymentType().equalsIgnoreCase(paymentType))
                .findFirst()
                .orElse(null);
        double amount = invoice.getTotalAmount();
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
        }

        // 3. Utför betalningen via strategin (sätter t.ex. fakturan som betald)
        String message = strategy.processPayment(invoice, amount);

        // 4. Skapa och spara betalningshistoriken
        Payment payment = new Payment(invoice, amount, paymentType.toUpperCase());

        invoiceRepository.save(invoice);
        Payment savedPayment = paymentRepository.save(payment);

        logger.info("Payment of amount {} processed successfully using strategy [{}] for invoice ID {}",
                amount, paymentType, invoiceId);

        return message;
    }
}
