package com.wac.autocore.service;

import com.wac.autocore.dto.payment.PaymentSummaryDto;
import com.wac.autocore.exception.EntityNotFoundException;
import com.wac.autocore.mapper.PaymentMapper;
import com.wac.autocore.model.Invoice;
import com.wac.autocore.model.Payment;
import com.wac.autocore.repository.InvoiceRepository;
import com.wac.autocore.repository.PaymentRepository;
import com.wac.autocore.service.payment.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final Map<String, PaymentStrategy> strategies;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(Map<String, PaymentStrategy> strategies, PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.strategies = strategies;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<PaymentSummaryDto> getAllPayments() {
        return PaymentMapper.toSummaryDtoList(paymentRepository.findAll());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String processPayment(Long invoiceId, String paymentType) {
        // 1. Hämta fakturan från databasen
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("invoice.error.not_found")); // Anpassa nyckeln efter era properties

        // 2. Hitta rätt betalningsstrategi
        PaymentStrategy strategy = strategies.values().stream()
                .filter(s -> s.getPaymentType().equalsIgnoreCase(paymentType))
                .findFirst()
                .orElse(null);

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
        }

        BigDecimal amount = invoice.getTotalAmount();

        // 3. Utför betalningen via strategin (sätter t.ex. fakturan som betald)
        String messageKey = strategy.processPayment(invoice, amount);

        // 4. Skapa och spara betalningshistoriken
        Payment payment = new Payment(invoice, amount, paymentType.toUpperCase());

        invoiceRepository.save(invoice);
        paymentRepository.save(payment);

        logger.info("Payment of amount {} processed successfully using strategy [{}] for invoice ID {}",
                amount, paymentType, invoiceId);

        return messageKey;
    }
}